package fr.edf.jenkins.plugins.mac.ssh

import org.junit.Rule
import org.jvnet.hudson.test.JenkinsRule

import com.trilead.ssh2.ChannelCondition
import com.trilead.ssh2.Connection
import com.trilead.ssh2.Session

import fr.edf.jenkins.plugins.mac.ssh.connection.SSHConnectionFactory
import fr.edf.jenkins.plugins.mac.ssh.connection.SSHGlobalConnectionConfiguration
import fr.edf.jenkins.plugins.mac.ssh.connection.SSHUserConnectionConfiguration
import spock.lang.Specification

class SSHCommandLauncherTest extends Specification {
    
    @Rule
    JenkinsRule jenkins
    
    def "executeCommand should not throw exception"() {
        setup:
        String command = "test command"
        Session session = Stub(Session) {
            execCommand(command) >> null
            waitForCondition(ChannelCondition.EXIT_STATUS | ChannelCondition.EXIT_SIGNAL, 5000) >> 1
            close() >> null
            getExitSignal() >> "0"
            getExitStatus() >> 0
            getStdout() >> new ByteArrayInputStream(new String("out").getBytes())
            getStderr() >> new ByteArrayInputStream(new String("err").getBytes())
        }
        Connection conn = Stub(Connection) {
            openSession() >> session
        }
        GroovySpy(SSHConnectionFactory, global:true)
        1 * SSHConnectionFactory.getSshConnection(*_) >> conn
        SSHGlobalConnectionConfiguration connectionConfig = Mock(SSHGlobalConnectionConfiguration)
        when:
        String result = SSHCommandLauncher.executeCommand(connectionConfig, false, command)
        
        then:
        notThrown Exception
        result != null
        result == "out"
    }
    
    def "executeCommand should throw exception because exit status is 1"() {
        setup:
        String command = "test command"
        Session session = Stub(Session) {
            execCommand(command) >> null
            waitForCondition(ChannelCondition.EXIT_STATUS | ChannelCondition.EXIT_SIGNAL, 5000) >> 1
            close() >> null
            getExitSignal() >> "Error"
            getExitStatus() >> 1
            getStdout() >> new ByteArrayInputStream(new String("out").getBytes())
            getStderr() >> new ByteArrayInputStream(new String("err").getBytes())
        }
        
        Connection conn = Stub(Connection) {
            openSession() >> session
        }
        GroovySpy(SSHConnectionFactory, global:true)
        1 * SSHConnectionFactory.getSshConnection(*_) >> conn
        SSHGlobalConnectionConfiguration connectionConfig = Mock(SSHGlobalConnectionConfiguration)
        when:
        String result = SSHCommandLauncher.executeCommand(connectionConfig, false, command)
        
        then:
        Exception e = thrown()
        e.getMessage() == "Failed to execute command " + command
    }
    
    def "executeCommand should not throw exception with exit status is 1 because ignore error is true"() {
        setup:
        String command = "test command"
        Session session = Stub(Session) {
            execCommand(command) >> null
            waitForCondition(ChannelCondition.EXIT_STATUS | ChannelCondition.EXIT_SIGNAL, 5000) >> 1
            close() >> null
            getExitSignal() >> "Error"
            getExitStatus() >> 1
            getStdout() >> new ByteArrayInputStream(new String("").getBytes())
            getStderr() >> new ByteArrayInputStream(new String("err").getBytes())
        }
        Connection conn = Stub(Connection) {
            openSession() >> session
        }
        GroovySpy(SSHConnectionFactory, global:true)
        1 * SSHConnectionFactory.getSshConnection(*_) >> conn
        SSHGlobalConnectionConfiguration connectionConfig = Mock(SSHGlobalConnectionConfiguration)
        when:
        String result = SSHCommandLauncher.executeCommand(connectionConfig, true, command)
        
        then:
        notThrown Exception
        result == "err"
    }
    
    def "sendFile should throw Exception because SCPClient cannot find host" () {
        setup:
        SSHUserConnectionConfiguration connectionConfig = Mock(SSHUserConnectionConfiguration)
        Connection conn = Stub(Connection) {
            close() >> {}
        }
        GroovySpy(SSHConnectionFactory, global:true)
        1 * SSHConnectionFactory.getSshConnection(*_) >> conn
        InputStream content = new ByteArrayInputStream(new String().getBytes())
        String fileName = "fileName"
        String outputDir = "outputDir"
        
        when:
        SSHCommandLauncher.sendFile(connectionConfig, content, fileName, outputDir)
        
        then:
        Exception e = thrown()
    }
}
