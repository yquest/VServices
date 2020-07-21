import groovyx.net.http.HttpBuilder
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class VertexServicesTest extends Specification {
    static private HttpBuilder http
    static private deployedServiceNames = []

    @SuppressWarnings("unused")//used by sposk
    def setupSpec() {
        http = ServerStarted.loadHttp()
    }

    @SuppressWarnings("unused") //used in reflection
    def cleanupSpec() {
        http.close()
    }

    def "deploy and redeploy service #service"() {
        Map<String, Object> resultDeploy = http.get { request.uri.path = "/deploy/$service" } as Map<String, String>
        Map<String, Object> resultRedeploy = http.get { request.uri.path = "/redeploy/$service" } as Map<String, String>
        def deployedServices = http.get { request.uri.path = '/deployed' } as Map<String, String>
        deployedServiceNames += service

        expect:
        resultDeploy.containsKey(service)
        resultRedeploy.containsKey(service)
        dependencies.every { deployedServices.containsKey(it) }

        where:
        service     | dependencies
        'routeJava' | ['server', 'sharedWeb']
    }

    def "undeploy service #service"() {
        given:

        http.get { request.uri.path = "/undeploy/$service" }
        def deployedServices = http.get { request.uri.path = '/deployed' } as Map<String, String>

        expect:
        !deployedServices.containsKey(service)

        where:
        service      | _
        'routeJava' | _

    }

}