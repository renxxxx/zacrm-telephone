package com.njshangka.zacrm;

import com.alibaba.fastjson.JSONObject;
import org.apache.log4j.Logger;
import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.enums.ReadyState;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.URISyntaxException;

@SpringBootApplication
@RestController
public class ZacrmTelephoneApplication {
    private static Logger logger = Logger.getLogger(ZacrmTelephoneApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ZacrmTelephoneApplication.class, args);
    }

    public ZacrmTelephoneApplication() throws URISyntaxException, InterruptedException {
        logger.info("in");
        initWebSocket();
    }

    @PostMapping("/send")
    public JSONObject send(@RequestParam(value = "message") String message) {
        try {
            if(message != null && !message.isEmpty())
                Config.webSocketClient.send(message);
            JSONObject result = new JSONObject(true);
            JSONObject resultData = new JSONObject(true);
            result.put("code", 0);
            result.put("codeMsg", null);
            result.put("data", resultData);
            String resultString = result.toJSONString();
            logger.info("resultString : " + resultString);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new JSONObject(true);
    }

    @GetMapping("/receive")
    public JSONObject receive() {
        try {
            JSONObject result = new JSONObject(true);
            JSONObject resultData = new JSONObject(true);
            resultData.put("value", Config.webSocketClientLastReceive);
            result.put("code", 0);
            result.put("codeMsg", null);
            result.put("data", resultData);
            String resultString = result.toJSONString();
            logger.info("resultString : " + resultString);

            Config.webSocketClientLastReceive = null;
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new JSONObject(true);
    }

    public void initWebSocket() throws URISyntaxException, InterruptedException {
        logger.info("in");
        String uri = "ws://localhost:1080?sid=" + ((int) ((1 + Math.random()) * 10000)) + "&pid=84529FA7-7195-4541-AA38-B22003CCFF4D&flag=1";
        logger.info("uri " + uri);

        Config.webSocketClient = new WebSocketClient(new URI(uri), new Draft_6455()) {

            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                logger.info("连接成功");

            }

            @Override
            public void onMessage(String msg) {
                logger.info("收到消息 : " + msg);
                if (msg != null && !msg.contains("HP_MsgEvent"))
                    Config.webSocketClientLastReceive = msg;
                if (msg.equals("over")) {
                    this.close();
                }
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                logger.info("链接已关闭");
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
                logger.info("发生错误已关闭");
            }
        };


        Config.webSocketClient.connect();
        logger.info(Config.webSocketClient.getDraft());
        while (!Config.webSocketClient.getReadyState().equals(ReadyState.OPEN)) {
            logger.info("正在连接...");
            Thread.sleep(1000);
        }

    }

}
