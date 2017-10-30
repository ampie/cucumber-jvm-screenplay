package com.sbg.bdd.cucumber.screenplay.core.formatter;

import gherkin.deps.net.iharder.Base64;
import gherkin.formatter.Argument;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MapParser {
    private final Reporter reporter;
    private final Formatter formatter;

    public MapParser(Reporter reporter, Formatter formatter) {
        this.reporter = reporter;
        this.formatter = formatter;
    }

    public void replayExamples(Map eo) {
        new Examples(comments(eo), tags(eo), keyword(eo), name(eo), description(eo), line(eo), id(eo), examplesTableRows(getList(eo, "rows"))).replay(formatter);
    }

    public void replayFeatureElement(Map featureElement) {
        featureElement(featureElement).replay(formatter);
    }

    public void replayFeature(Map o) {
        formatter.uri(getString(o,"uri"));
        new Feature(comments(o), tags(o), keyword(o), name(o), description(o), line(o), id(o)).replay(formatter);
    }

    private BasicStatement featureElement(Map o) {
        String type = (String) o.get("type");
        if (type.equals("background")) {
            return new Background(comments(o), keyword(o), name(o), description(o), line(o));
        } else if (type.equals("scenario")) {
            return new Scenario(comments(o), tags(o), keyword(o), name(o), description(o), line(o), id(o));
        } else if (type.equals("scenario_outline")) {
            return new ScenarioOutline(comments(o), tags(o), keyword(o), name(o), description(o), line(o), id(o));
        } else {
            return null;
        }
    }

    public void replayBefore(Map o) {
        Match match = buildMatch(o);
        Map r = (Map) o.get("result");
        Result result = new Result(status(r), duration(r), errorMessage(r));
        reporter.before(match, result);
    }

    public void replayAfter(Map o) {
        Match match = buildMatch(o);
        Map r = (Map) o.get("result");
        Result result = new Result(status(r), duration(r), errorMessage(r));
        reporter.after(match, result);
    }

    public void replayStepAndMatch(Map o) {
        replayStep(o);
        if (o.containsKey("match")) {
            buildMatch(o).replay(reporter);
        }
    }

    public void replayStep(Map o) {
        buildStep(o).replay(formatter);
    }

    private Match buildMatch(Map o) {
        Map m = (Map) o.get("match");
        return new Match(arguments(m), location(m));
    }

    private Step buildStep(Map o) {
        List<DataTableRow> rows = null;
        if (o.containsKey("rows")) {
            rows = dataTableRows(getList(o, "rows"));
        }
        DocString docString = null;
        if (o.containsKey("doc_string")) {
            Map ds = (Map) o.get("doc_string");
            docString = new DocString(getString(ds, "content_type"), getString(ds, "value"), getInt(ds, "line"));
        }

        return new Step(comments(o), keyword(o), name(o), line(o), rows, docString);
    }
    public void replayMatchAndResult(Map step){
        buildMatch(step).replay(reporter);
        replayResult((Map) step.get("result"));
    }
    public void replayResult(Map r) {
        new Result(status(r), duration(r), errorMessage(r)).replay(reporter);
    }

    public void replayEmbedding(Map embedding) {
        try {
            reporter.embedding(getString(embedding, "mime_type"), Base64.decode(getString(embedding, "data")));
        } catch (IOException ex) {
            throw new RuntimeException("Couldn't decode data", ex);
        }
    }

    private List<DataTableRow> dataTableRows(List o) {
        List<DataTableRow> rows = new ArrayList<DataTableRow>(o.size());
        for (Object e : o) {
            Map row = (Map) e;
            rows.add(new DataTableRow(comments(row), getList(row, "cells"), getInt(row, "line")));
        }
        return rows;
    }

    private List<ExamplesTableRow> examplesTableRows(List o) {
        List<ExamplesTableRow> rows = new ArrayList<ExamplesTableRow>(o.size());
        for (Object e : o) {
            Map row = (Map) e;
            rows.add(new ExamplesTableRow(comments(row), getList(row, "cells"), getInt(row, "line"), id(row)));
        }
        return rows;
    }

    private List<Comment> comments(Map o) {
        List<Comment> comments = new ArrayList<Comment>();
        if (o.containsKey("comments")) {
            for (Object e : ((List) o.get("comments"))) {
                Map map = (Map) e;
                comments.add(new Comment(getString(map, "value"), getInt(map, "line")));
            }
        }
        return comments;
    }

    private List<Tag> tags(Map o) {
        List<Tag> tags = new ArrayList<Tag>();
        if (o.containsKey("tags")) {
            for (Object e : ((List) o.get("tags"))) {
                Map map = (Map) e;
                tags.add(new Tag(getString(map, "name"), getInt(map, "line")));
            }
        }
        return tags;
    }

    private String keyword(Map o) {
        return getString(o, "keyword");
    }

    private String name(Map o) {
        return getString(o, "name");
    }

    private String description(Map o) {
        return getString(o, "description");
    }

    private Integer line(Map o) {
        return getInt(o, "line");
    }

    private String id(Map o) {
        return getString(o, "id");
    }

    private List<Argument> arguments(Map m) {
        List arguments = getList(m, "arguments");
        List<Argument> result = new ArrayList<Argument>();
        for (Object argument : arguments) {
            Map argMap = (Map) argument;
            result.add(new Argument(getInt(argMap, "offset"), getString(argMap, "val")));
        }
        return result;
    }

    private String location(Map m) {
        return getString(m, "location");
    }

    private String status(Map r) {
        return getString(r, "status");
    }

    private Long duration(Map r) {
        return getLong(r, "duration");
    }

    private String errorMessage(Map r) {
        return getString(r, "error_message");
    }

    private String getString(Map map, String key) {
        Object string = map.get(key);
        return string == null ? null : (String) string;
    }

    private Integer getInt(Map map, String key) {
        Object n = map.get(key);
        return n == null ? null : ((Number) n).intValue();
    }

    private Long getLong(Map map, String key) {
        Object n = map.get(key);
        return n == null ? null : ((Number) n).longValue();
    }

    private List getList(Map map, String key) {
        Object list = map.get(key);
        return list == null ? Collections.emptyList() : (List) list;
    }

    public void replayChildResult(Map<String, Object> r) {
        if(reporter instanceof ScreenPlayFormatter){
            ((ScreenPlayFormatter)reporter).childResult(new Result(status(r), duration(r), errorMessage(r)));
        }
    }

    public void replayChildStepAndMatch(Map<String, Object> stepAndMatch) {
        if(reporter instanceof ScreenPlayFormatter){
            ((ScreenPlayFormatter)reporter).childStep(buildStep(stepAndMatch),buildMatch(stepAndMatch));
        }
    }
}
