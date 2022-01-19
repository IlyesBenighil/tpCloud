package org.example.abd;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.sql.Time;

import org.example.abd.cmd.Command;
import org.example.abd.cmd.CommandFactory;
import org.example.abd.cmd.ReadReply;
import org.example.abd.cmd.ReadRequest;
import org.example.abd.cmd.WriteReply;
import org.example.abd.cmd.WriteRequest;
import org.example.abd.quorum.Majority;
import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.jgroups.util.Util;

public class RegisterImpl<V> extends ReceiverAdapter implements Register<V>{

    private String name;

    private CommandFactory<V> factory;
    private JChannel channel;

    private V value;
    private int label;
    private int max;
    private Majority quorumSystem;
    private boolean ack;
    private int nbAck;
    private V maxValue;
    private boolean isWritable;

    public RegisterImpl(String name) {
        this.name = name;
        this.factory = new CommandFactory<>();
        
        
    }

    public void init(boolean isWritable) throws Exception{
    this.isWritable = isWritable;
    nbAck = 0;
    ack = false;
    value = null;
    label = 0;
    channel = new JChannel().receiver(this);
    channel.connect("chan");
    
    this.quorumSystem = new Majority(channel.getView());
    if( isWritable){
        this.max = 0;
    } else{
        this.max = -1;
    }
    
    }

    
    public void viewAccepted(View view) {
        this.quorumSystem = new Majority(view);

    }

    // Client part

    @Override
    public V read() {
        ReadRequest<V> rqst = factory.newReadRequest();
        execute(rqst);
        System.out.println("debut d'attendre");
        Util.sleep(2000);
        System.out.println("fini d'attendre");
    return maxValue;
    }

    @Override
    public void write(V v) {
        label = label + max;
        Command<V> cmd = factory.newWriteRequest(v, label);
        label++;
        execute(cmd);
        Util.sleep(2000);
        ack = false;
        nbAck = 0;

     }

    

    private synchronized V execute(Command<V> cmd){  
        CompletableFuture<Void> mirai =  CompletableFuture.runAsync(() -> {
            ArrayList<Address> liste = (ArrayList<Address>) quorumSystem.pickQuorum();
            System.out.println(liste.contains(channel.getAddress()));
            System.out.println("LA LISTE EST / " + liste);
            for (int i = 0; i < liste.size(); i++){
                System.out.println(cmd.getClass() + " à " + liste.get(i));
                send(liste.get(i), cmd);
                Util.sleep(300);
            }
        });
        return null;
    }

    // Message handlers
    @Override
    public void receive(Message msg) {
        if( msg.getObject() instanceof WriteRequest){
            System.out.println("name :  " + this.channel.getName() + " value : " + this.value + " label : " + this.label);
            WriteRequest<V> cmd = (WriteRequest<V>) msg.getObject();
            if (this.label <= cmd.getTag() ){
                this.label = cmd.getTag();
                this.value = cmd.getValue();
                System.out.println("J'ai écrit la valeur : " + cmd.getValue() + " chez : " + this.channel.getName());
            }            
            Command<V> cmdReply = factory.newWriteReply();
            send(msg.getSrc(),cmdReply);
        }else if(msg.getObject() instanceof WriteReply){
            nbAck = nbAck + 1;
            if(nbAck == quorumSystem.quorumSize()){
                ack = true;
            }
        }else if(msg.getObject() instanceof ReadRequest){
        ReadReply<V> reply = factory.newReadReply((V) this.value, this.label);
        send(msg.getSrc(),reply);
         }else if(msg.getObject() instanceof ReadReply){
            nbAck = nbAck + 1;
            if(nbAck == quorumSystem.quorumSize()){
                ack = true;
            }
            ReadReply<V> rep = (ReadReply) msg.getObject();
            if(this.label < rep.getTag()){
                this.maxValue =  rep.getValue();
                this.label = rep.getTag();
            } else{
                this.maxValue = this.value;
            }


         }
}

    private void send(Address dst, Command<V> command) {
        System.out.println("Je send à dst : " + dst + " la cmd: " + command.getClass());
        try {
            Message message = new Message(dst,channel.getAddress(), command);
            channel.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
