package com.sbg.bdd.android.screenplay.fileserver

import spock.lang.Specification


class WhenManipulatingFilesRemotely extends Specification {
    static FileServer server
    static File rootFile

    def setupSpec() {
        if(server ==null) {
            def file = Thread.currentThread().contextClassLoader.getResource('android-screenplay-marker.txt').file

            rootFile = new File(file).parentFile
            server = new FileServer(rootFile, 9999)
            server.start()
        }
    }

    def cleanupSpec() {
        server.stop();
    }

    def 'it should return the files in the specified dir alphabetically'() {

        given:
        def client = new FileClient(InetAddress.getLocalHost().hostName, 9999)
        def root = new FileServerResourceRoot(client)

        when:
        def list = root.list()

        then:
        list.length == 3
        list[0].name == 'android-screenplay-marker.txt'
        list[0] instanceof FileServerReadableResource
        list[1].name == 'dir1'
        list[1] instanceof FileServerResourceContainer
        list[2].name == 'dir2'
        list[2] instanceof FileServerResourceContainer
    }

    def 'it should read the file in the specified path'() {

        given:
        def client = new FileClient(InetAddress.getLocalHost().hostName, 9999)
        def root = new FileServerResourceRoot(client)

        when:
        def file = root.resolveExisting('dir1', 'dir1_1/file1.txt')

        then:
        new String(file.read()) == 'file1_content'
    }

    def 'it should write the file in the specified path'() {

        given:
        def client = new FileClient(InetAddress.getLocalHost().hostName, 9999)
        def root = new FileServerResourceRoot(client)
        def actualFile = new File(rootFile, 'dir1/dir1_1/file2.txt')
        if(actualFile.exists()){
            actualFile.delete()
        }
        def fileServerWritableResource = root.resolvePotential('dir1', 'dir1_1/file2.txt')


        def expectedContent = "expected_content".getBytes()
        when:
        fileServerWritableResource.write(expectedContent)


        then:
        println actualFile
        def fis = new FileInputStream(actualFile)
        def foundContent = new byte[expectedContent.length]
        fis.read(foundContent)
        foundContent == expectedContent
    }


}