import groovyx.net.http.HttpBuilder
import spock.lang.Specification
import spock.lang.Title
import spock.lang.Unroll

@Unroll
@Title("that's a cool title")
class VertexServicesTest extends Specification {
    static private HttpBuilder http

    //used by spock
    @SuppressWarnings("unused")
    def setupSpec() {
        http = ServerStarted.loadHttp()
    }

    @SuppressWarnings("unused")
    //used by spock
    def cleanupSpec() {
        http.close()
    }

    def "deploy and redeploy service #service"() {
        Map<String, Object> resultDeploy = http.get { request.uri.path = "/deploy/$service" } as Map<String, String>
        sleep(2000)
        Map<String, Object> resultRedeploy = http.get { request.uri.path = "/redeploy/$service" } as Map<String, String>
        def deployedServices = http.get { request.uri.path = '/deployed' } as Map<String, String>

        expect:
        resultDeploy.containsKey(service)
        dependencies.every { deployedServices.containsKey(it) }
        resultRedeploy.containsKey(service)
        dependencies.every { deployedServices.containsKey(it) }

        where:
        service          | dependencies
        'groovyRoute'    | ['server', 'sharedWeb']
        'routeJava'      | ['server', 'sharedWeb']
        'groovyVerticle' | ['server', 'sharedWeb']
    }

    def "error when deploy the service #service twice"() {
        http.get { request.uri.path = "/deploy/$service" } as Map<String, String>
        Map<String, Object> result2ndTime = http.get { request.uri.path = "/deploy/$service" } as Map<String, String>

        expect:
        result2ndTime == [error: "the servcie '$service' is already installed".toString()]

        where:
        service       | _
        'groovyRoute' | _
        'routeJava'   | _
    }

    def "undeploy service #service"() {
        given:
        def deployedServices = http.get { request.uri.path = '/deployed' } as Map<String, String>
        if (!deployedServices.containsKey(service)) {
            http.get { request.uri.path = "/deploy/$service" } as Map<String, String>
        }
        http.get { request.uri.path = "/undeploy/$service" }
        deployedServices = http.get { request.uri.path = '/deployed' } as Map<String, String>

        expect:
        !deployedServices.containsKey(service)

        where:
        service       | _
        'routeJava'   | _
        'groovyRoute' | _
    }

}