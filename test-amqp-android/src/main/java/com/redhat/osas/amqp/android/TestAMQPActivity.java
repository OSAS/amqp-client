package com.redhat.osas.amqp.android;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.redhat.osas.amqp.client.api.Connection;
import com.redhat.osas.amqp.client.api.Message;
import com.redhat.osas.amqp.client.api.Sender;
import com.redhat.osas.amqp.client.api.Session;
import com.redhat.osas.amqp.client.impl.MessageImpl;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestAMQPActivity extends Activity {
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        EditText ip = (EditText) findViewById(R.id.ip);
        Button button = (Button) findViewById(R.id.sendButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ExecutorService executorService = Executors.newSingleThreadExecutor();
                executorService.submit(new Runnable() {
                    public void run() {
                        Connection c = new Connection("192.168.1.115", (short) 5672);
                        c.open();
                        Session session = c.createSession(UUID.randomUUID().toString());
                        Sender sender = session.createSender("");
                        sender.setCapacity(10000);
                        Message message = new MessageImpl();
                        message.setSubject("test");
                        message.setContent("hello, world");
                        sender.send(message);
                        session.sync();
                        session.close();
                    }
                });
            }
        });
    }
}