package fr.edf.jenkins.plugins.mac

import org.junit.Rule
import org.jvnet.hudson.test.JenkinsRule

import fr.edf.jenkins.plugins.mac.test.builders.MacPojoBuilder
import hudson.model.FreeStyleProject
import hudson.model.Label
import spock.lang.Specification

class MacCloudTest extends Specification {

    @Rule
    JenkinsRule jenkinsRule

    def "should create cloud"() {
        setup:
        MacCloud cloud = MacPojoBuilder.buildMacCloud(MacPojoBuilder.buildMacHost(), MacPojoBuilder.buildConnector(jenkinsRule))

        when:
        jenkinsRule.jenkins.clouds.add(cloud)

        then:
        notThrown Exception
        jenkinsRule.jenkins.clouds.size() == 1
        cloud == jenkinsRule.jenkins.getCloud("test")
    }

    def "should call provision method"() {
        setup:
        MacCloud cloud = MacPojoBuilder.buildMacCloud(MacPojoBuilder.buildMacHost(), MacPojoBuilder.buildConnector(jenkinsRule))
        jenkinsRule.jenkins.clouds.add(cloud)
        FreeStyleProject project = jenkinsRule.createFreeStyleProject("test")
        project.setAssignedLabel(Label.parse("testLabel").getAt(0))

        when:
        boolean isBuilt = project.scheduleBuild2(1)
        //TODO wait until MacCloud.provision is called
        then:
        notThrown Exception
        isBuilt == true
    }
    
}
