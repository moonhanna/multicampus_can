package com.example.pad2;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pad2.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import msg.Msg;

public class MainActivity extends AppCompatActivity {

    TextView tv, tvclient, servertv;

    Socket ssocket;
    String sip = "70.12.113.248"; // my computer
    //    String sip = "70.12.231.175";
    int sport = 7777;

    public static HashMap<String, ObjectOutputStream>
            maps = new HashMap<String, ObjectOutputStream>();
    HashMap<String, String>
            ids = new HashMap<String, String>();
    ServerSocket serverSocket;

    int port = 8888;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        makeUi();

    }

    class newClient extends Thread {

        String ip;

        newClient(){

        }

        public void setIp(String ip){
            this.ip = ip;
        }



        String urlstr = "http://70.12.231.175:8088/semipj/newClient?"
                + "id=" ;

        @Override
        public void run() {
            try {
                urlstr+=ip;
                Log.d("-----", urlstr);
                URL url = new URL(urlstr);
                HttpURLConnection con =
                        (HttpURLConnection) url.openConnection();
                con.getInputStream();

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }


    class serverReady extends Thread{

        public serverReady(){
            try {
                serverSocket = new ServerSocket(port);
                Log.d("-----","ServerSocket created..");

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            while(true) {

                Socket socket = null;


                try {
                    Log.d("-----","Server Ready..");
                    socket = serverSocket.accept();
                    newClient nc = new newClient();
                    nc.setIp(socket.getInetAddress().toString());
                    nc.start();



                    Log.d("-----","client connected..");

                    if(socket.getInetAddress().toString().equals("/70.12.113.202")){
                        final Socket finalSocket = socket;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                tvclient.setText("Connected"+ finalSocket.getInetAddress().toString());
                            }
                        });
                    }else if(socket.getInetAddress().toString().equals("/70.12.113.206")){
                        final Socket finalSocket = socket;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                tvclient.setText("Connected"+ finalSocket.getInetAddress().toString());
                            }
                        });
                    }
                    else  if(socket.getInetAddress().toString().equals("/70.12.113.248")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                servertv.setText("Connected");
                            }
                        });
                    }




                    new Receiver(socket).start();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void setList(){
//        adapter =
//                new ArrayAdapter<String>(
//                        MainActivity.this,
//                        android.R.layout.simple_list_item_1,
//                        getIds()
//                );
//        adapter.notifyDataSetChanged();
//        listView.setAdapter(adapter);
    }

    private void makeUi() {
        tvclient = findViewById(R.id.tvclient);
        servertv = findViewById(R.id.servertv);
        tv = findViewById(R.id.tv);

        new ConnectThread(sip,sport,null).start();

    }

    public void ckbt(View v){
        if(v.getId() == R.id.startbt){

            Msg msg = new Msg("Admin","start",null);
            new Sender(msg).start();
            new serverReady().start();



        }else if(v.getId() == R.id.endbt){
            Msg msg = new Msg("Admin","stop",null);
            new Sender(msg).start();
        }

    }

    public void displayData(Msg msg){
        final String txt = msg.getTxt();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                tv.setText(txt);
            }
        });


    }

    class Receiver extends Thread{

        InputStream is;
        ObjectInputStream ois;

        OutputStream os;
        ObjectOutputStream oos;

        Socket socket;
        public Receiver(Socket socket) throws IOException {
            this.socket = socket;
            is = socket.getInputStream();
            ois = new ObjectInputStream(is);

            os = socket.getOutputStream();
            oos = new ObjectOutputStream(os);
            maps.put(socket.getInetAddress().toString(),
                    oos);
            try {
                Log.d("-----","receive thread");
                Msg msg = (Msg) ois.readObject();

                ids.put(socket.getInetAddress().toString(),
                        msg.getId());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            while(ois != null) {
                Msg msg = null;
                try {

                    msg = (Msg) ois.readObject();
                    if(msg.getTxt().equals("q")) {
                        System.out.println(
                                ids.get(socket.getInetAddress().toString())+":Exit ..");

                        maps.remove(
                                socket.getInetAddress().toString()
                        );

                        ids.remove(socket.getInetAddress().toString()
                        );
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // setList();
                            }
                        });
                        break;
                    }
                    //sendMsg(msg);
                    Log.d("-----",msg.getId()+" "+msg.getTxt()+" "+msg.getTid());
                    final Msg fimsg = msg;
                    if(msg.getTid() != null)
                        new Thread(new Runnable() {
                            String urlstr = "http://70.12.231.175:8088/semipj/receivepad?"
                                    +"id="+fimsg.getId()
                                    +"&txt="+fimsg.getTxt()
                                    +"&tid="+fimsg.getTid()
                                    ;




                            @Override
                            public void run() {
                                try {
                                    Log.d("----", urlstr);
                                    URL url = new URL(urlstr);
                                    HttpURLConnection con =
                                            (HttpURLConnection) url.openConnection();
                                    con.getInputStream();

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            }
                        }).start();

                    displayData(msg);
                } catch (Exception e) {
                    maps.remove(
                            socket.getInetAddress().toString()
                    );

                    ids.remove(socket.getInetAddress().toString()
                    );
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //   setList();
                        }
                    });
                    break;
                }
            } // end while
            try {
                if(ois != null) {
                    ois.close();
                }
                if(socket != null) {
                    socket.close();
                }
            }catch(Exception e) {
                e.printStackTrace();
            }
        }

    }

    class Sender extends Thread{
        Msg msg;
        public Sender(Msg msg) {
            this.msg = msg;
        }
        @Override
        public void run() {
            if( msg.getTid()==null||msg.getTid().equals("/") || msg.getTid().equals("")){
                Collection<ObjectOutputStream>
                        cols = maps.values();
                Iterator<ObjectOutputStream>
                        its = cols.iterator();
                while(its.hasNext()) {
                    try {
                        its.next().writeObject(msg);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
            else{
                try {
                    maps.get(msg.getTid()).writeObject(msg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    class ConnectThread extends Thread {

        String ip;
        int port;
        String id; //id 추가

        OutputStream os;
        ObjectOutputStream oos;

        public ConnectThread() {
        }

        public ConnectThread(String ip, int port, String id) {
            this.ip = ip;
            this.port = port;
            //
            this.id = id;


        }

        @Override
        public void run() {

            //Client.java의 Client()에서 try/catch가져오기
            try {
                Log.d("--------","run");
//                Thread.sleep(1000);
                // ssocket.setSoTimeout(2000);
                ssocket = new Socket(ip, port); //소켓만들고
                os = ssocket.getOutputStream();
                oos = new ObjectOutputStream(os);
                Msg test = new Msg("test","test",null);
                oos.writeObject(test);
                Log.d("--------","run2");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        servertv.setText("Connected Server"); //현상황찍고
                        //new Sender(new Msg("tablet","asdf",null)).start();


                    }
                });


            } catch (Exception e) {
                Log.d("--------","ex");
                int i =0;

                while (true) {
                    i++; //몇번 retry했는지 표시
                    Log.d("----",i+"");
                    e.printStackTrace();
                    final int finalI = i;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            servertv.setText("Retry Connection"+ finalI); //현상황찍고
                        }
                    });
                    //System.out.println("Retry.."); : 안드로이드에선 사용불가(log.d)
                    //현재 화면에 있는 text를 가져와서 뿌린다.
                    try {
                        Thread.sleep(1000);
//                        ssocket.setSoTimeout(2000);
                        ssocket = new Socket(ip, port); //커넥션시도
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                servertv.setText("Connected Server"); //현상황찍고
                                new Sender(new Msg("tablet","asdf",null)).start();

                            }
                        });

                    } catch (Exception e1) {
                        e1.printStackTrace();


                    }
                }
            }
            //retry일때도 sender가 형성되어야하므로 여기에 생성
            try {

                //객체생성
                SReceiverTask sreceiverTask = new SReceiverTask(ssocket); //리시버만들고
                sreceiverTask.execute();


            } catch (IOException e) {
                e.printStackTrace();
            }
        } //end run

    }

    class SReceiverTask extends AsyncTask<Void, Msg, Void> {

        InputStream is;
        ObjectInputStream ois;

        public SReceiverTask(Socket socket) throws IOException {
            is = socket.getInputStream();
            ois = new ObjectInputStream(is);
        }


        @Override
        protected Void doInBackground(Void... voids) {
            while (ois != null) {
                Msg msg = null;
                try {
                    msg = (Msg) ois.readObject();
                    publishProgress(msg);
                } catch (Exception e) {
                    msg = new Msg("System", "Server is dead", null);
                    publishProgress(msg);
                    break;

                }
            }
            return null;

        }


        @Override
        protected void onPostExecute(Void aVoid) {
            try {
                if (ois != null) {
                    ois.close();
                }
                if (ssocket != null) {
                    ssocket.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        //doinbackground가 동작되는 동안 실행(던져주면받음)
        @Override
        protected void onProgressUpdate(final Msg... values) {
            Log.d("--------",values[0].getId()+" "+values[0].getTid());
            String id = values[0].getId();
            if(id.equals("Admin")){
                String txt = values[0].getTxt(); //서버에서 "1", "2" 이런식으로 온다.
                tv.setText(txt);//받았는지 확인
                if(values[0].getTid()!=null)
                    new Sender(values[0]).start();
//                if(ssocket != null){
//                    try {
//                        ssocket.close();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                        new ConnectThread(sip,sport,null).start();
//                    }
//                }

                // 만약 패드와서버가 통신중에 서버가 죽으면 다시 reconnection을 요구한다.
                //일단 소켓을 close 하고 다시 thread를 돌려 reconnection 하는 것이다.
                //    return;


            }


            Msg msg = null;

//            if(txt.trim().equals("0")){
//                msg = new Msg("server","0",null);
//            }else{
//                msg = new Msg("server","1",null);
//            }
//            sendMsg(msg);


        }


    }



}
