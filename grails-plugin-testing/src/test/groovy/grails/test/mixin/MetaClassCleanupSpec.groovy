package grails.test.mixin

import spock.lang.Specification
import grails.test.mixin.support.GrailsUnitTestMixin
import org.junit.After
import org.junit.AfterClass
import org.junit.Assert

/**
 * Created by IntelliJ IDEA.
 * User: graemerocher
 * Date: 11/14/11
 * Time: 2:49 PM
 * To change this template use File | Settings | File Templates.
 */
@TestMixin(GrailsUnitTestMixin)
class MetaClassCleanupSpec extends Specification{

    def "Test that meta classes are restored to prior state after test run"() {
        when:"A meta class is modified in the test"
            Author.metaClass.testMe = {-> "test"}
            def a = new Author()
        then:"The method is available"
            a.testMe() == "test"
    }

    def instance = new Author()
    def "Test that changes made to an instance are cleaned up - step 1"() {
        when:"a change is made to an instance"
            instance.metaClass.doWork = {->"done"}

        then:"The method is callable"
            instance.doWork() == "done"
    }

    def "Test that changes made to an instance are cleaned up - step 2"() {
        when:"when the method is called again"
            instance.doWork()

        then:"The method was cleaned by the registry cleaner"
            thrown MissingMethodException
    }

    def "Test that meta class cleanup doesn't break XmlSlurper - step 1"() {
        given:"A service that uses XmlSlurper"
            def service = new HelloService()
        when:"A method is invoked that uses XmlSlurper"
            def greeting = service.greet('hello')
        then:"The correct result is returned"
            greeting == 'hello'
    }

    def "Test that meta class cleanup doesn't break XmlSlurper - step 2"() {
        given:"A service that uses XmlSlurper"
        def service = new HelloService()
        when:"A method is invoked that uses XmlSlurper"
        def greeting = service.greet('goodbye')
        then:"The correct result is returned"
        greeting == 'goodbye'
    }

    @AfterClass
    static void checkCleanup() {
        def a = new Author()

        try {
            a.testMe()
            Assert.fail("Should have cleaned up meta class changes")
        } catch (MissingMethodException) {
        }
    }

}
class Author {
    String name
}

class HelloService {

    def greet(message) {
        def xml = "<greeting message='${message}'/>"

        new XmlParser().parseText(xml).@message
    }

}
