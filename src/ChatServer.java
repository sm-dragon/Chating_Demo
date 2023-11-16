import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;

/*
 Chatting 서버
 1. 클라이언트의 접속 -> ServerWorker Thread 생성 및 start
 2. ServerWorker -> 개별 client에 채팅 서비스

 */
public class ChatServer {
    private ArrayList<ServerWorker> list = new ArrayList<ServerWorker>();

    public void go() throws IOException {
        ServerSocket serverSocket = null;
        try {

            // 채팅 서버 시작
            serverSocket = new ServerSocket(5432);
            System.out.println("**ChatServer Start**");

            // 다수의 클라이언트에게 지속적으로 서비스하기 위해 while 이용
            while (true) {
                Socket socket = serverSocket.accept();
                ServerWorker sw = new ServerWorker(socket);
                list.add(sw);
                Thread thread = new Thread(sw);
                thread.start();
            }

        } finally {
            if (serverSocket != null)
                serverSocket.close();
            System.out.println("**ChatServer End**");
        }
    }

    public void sendMessage(String message) {
        System.out.println(message);
        // 접속해 있는 모든 클라이언트들에게 메세지 전송
        for (int i=0;i<list.size();i++) {
            list.get(i).pw.println(message);
        }
    }

    class ServerWorker implements Runnable {
        private Socket socket;
        private BufferedReader br;
        private PrintWriter pw;
        private String user;

        public ServerWorker(Socket socket) {
            super();
            this.socket = socket;
            user = socket.getInetAddress().toString().replaceAll("/","");
        }
        public void chatting() throws IOException {
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            pw = new PrintWriter(socket.getOutputStream(),true);
            sendMessage(user+"님이 입장하셨습니다");

            //로그 생성 영역 시작
            LocalDateTime currentDateTime = LocalDateTime.now();
            String filePath = "Test.txt";

            File file = new File(filePath); // File객체 생성
            if(!file.exists()){ // 파일이 존재하지 않으면
                file.createNewFile(); // 신규생성
            }

            // BufferedWriter 생성
            BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));

            //로그 생성 영역 끝

            try {
                while (true) {
                    String message = br.readLine();
                    if (message.trim().equals("종료") || message.equals("null") || message == null) {

                        break;
                    }
                    sendMessage(user+"님:"+message);

                    //여기다 로그 남기기
                    writer.write("[" + currentDateTime +"] " + "[" + user + "] " + "[" + message + "]");
                    writer.newLine();
                    writer.flush();

                } // while
            } // echo method
            catch (Exception e) {
            }
        }

        public void run() {
            try {
                chatting();

            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                try {
                    closeAll();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                sendMessage(user+"님이 나가셨습니다!!");
                list.remove(this);
            }
        }

        public void closeAll() throws IOException {
            if (pw != null)
                pw.close();
            if (br != null)
                br.close();
            if (socket != null)
                socket.close();
        }

    }

    public static void main(String[] args) {
        try {
            new ChatServer().go();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}