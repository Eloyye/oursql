import lombok.extern.slf4j.Slf4j;
import transport.server.TCPServer;

void main() {
    try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
        var tcpServer = new TCPServer(executor);
        tcpServer.listen();
    } catch (IOException e) {
        System.out.println("does not work");
    }
}
