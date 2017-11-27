package com.sbg.bdd.screenplay.scoped.junit;

import com.sbg.bdd.screenplay.core.actors.OnStage;
import com.sbg.bdd.screenplay.core.events.ScreenPlayEventBus;
import com.sbg.bdd.screenplay.core.internal.PersonaBasedCast;
import com.sbg.bdd.screenplay.core.internal.SimpleInstanceGetter;
import com.sbg.bdd.screenplay.scoped.FunctionalScope;
import com.sbg.bdd.screenplay.scoped.GlobalScope;
import com.sbg.bdd.screenplay.scoped.ScenarioScope;
import com.sbg.bdd.screenplay.scoped.UserTrackingScope;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ScopingRule implements TestRule {
    private static Logger LOGGER = Logger.getLogger(ScopingRule.class.getName());

    @Override
    public Statement apply(final Statement statement, final Description description) {
        return new Statement() {
            FunctionalScope packageScope = null;
            FunctionalScope classScope = null;
            ScenarioScope methodScope = null;

            @Override
            public void evaluate() throws Throwable {
                try {
                    try {
                        GlobalScope globalScope;
                        if (OnStage.performance() instanceof GlobalScope) {
                            globalScope = (GlobalScope) OnStage.performance();
                        } else {
                            globalScope = startDefaultGlobalScope();
                        }
                        if (OnStage.theCurrentScene() instanceof GlobalScope) {
                            startFirstPackageAndClassScope(globalScope);
                        } else {
                            assignCurrentPackageAndClassScope();
                            boolean needsNewClassScope = !classScope.getName().equals(description.getTestClass().getSimpleName());
                            if (needsNewClassScope) {
                                completeOldClassScope();
                            }
                            if (!packageScope.getName().equals(description.getTestClass().getPackage().getName())) {
                                changeToNewPackageScope(globalScope);
                            }
                            if (needsNewClassScope) {
                                startNewClassSCope();
                            }
                        }
                        methodScope = classScope.startScenario(description.getMethodName());
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING, "Could not start scope.", e);
                    }
                    statement.evaluate();
                } finally {
                    try {
                        classScope.completeNestedScope(methodScope.getName());
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING, "Could not complete scope.", e);
                    }
                }
            }

            private void startNewClassSCope() {
                classScope = packageScope.startNestedScope(description.getTestClass().getSimpleName());
            }

            private void changeToNewPackageScope(GlobalScope globalScope) {
                globalScope.completeNestedScope(packageScope.getName());
                packageScope = globalScope.startFunctionalScope(description.getTestClass().getPackage().getName());
            }

            private void completeOldClassScope() {
                packageScope.completeNestedScope(classScope.getName());
            }

            private void assignCurrentPackageAndClassScope() {
                classScope = (FunctionalScope) OnStage.theCurrentScene();
                packageScope = (FunctionalScope) classScope.getContainingScope();
            }

            private void startFirstPackageAndClassScope(GlobalScope globalScope) {
                packageScope = globalScope.startFunctionalScope(description.getTestClass().getPackage().getName());
                classScope = packageScope.startNestedScope(description.getTestClass().getSimpleName());
            }

            private GlobalScope startDefaultGlobalScope() {
                ScreenPlayEventBus screenPlayEventBus = new ScreenPlayEventBus(new SimpleInstanceGetter());
                screenPlayEventBus.scanClasses(Collections.<Class<?>>singleton(description.getTestClass()));
                final GlobalScope globalScope = new GlobalScope("test", new PersonaBasedCast(screenPlayEventBus, null, null), screenPlayEventBus);
                OnStage.present(globalScope);
                globalScope.start();
                Runtime.getRuntime().addShutdownHook(new Thread() {
                    @Override
                    public void run() {
                        while (globalScope.getInnerMostActive(UserTrackingScope.class) != globalScope) {
                            UserTrackingScope activeScope = globalScope.getInnerMostActive(UserTrackingScope.class);
                            activeScope.getContainingScope().completeNestedScope(activeScope.getName());
                        }
                        globalScope.complete();

                    }
                });
                return globalScope;
            }
        };
    }
}
