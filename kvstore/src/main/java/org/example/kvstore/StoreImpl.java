package org.example.kvstore;

import org.example.kvstore.cmd.Command;
import org.example.kvstore.cmd.CommandFactory;
import org.example.kvstore.cmd.Get;
import org.example.kvstore.cmd.Put;
import org.example.kvstore.cmd.Reply;
import org.example.kvstore.distribution.ConsistentHash;
import org.example.kvstore.distribution.Strategy;
import org.jgroups.Address;
import org.jgroups.Message;
import org.jgroups.View;
import org.jgroups.blocks.cs.ReceiverAdapter;

import java.nio.channels.Channel;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StoreImpl<K,V> extends ReceiverAdapter implements Store<K,V> {

  private class CmdHandler implements Callable<Void> {
    private Address dst;
    private Command<K,V> cmd;
    private Reply<K,V> rep;
    public CmdHandler(Address dst,Command<K,V> cmd){
      this.dst = dst;
      this.cmd = cmd;
    }
    @Override
    public Void call() throws Exception {
      V value = null;
      if (cmd.getKey() instanceof Get){
        value = get(cmd.getKey());
      }
      else if(cmd.getKey() instanceof Put) {
        value = put(cmd.getKey(),cmd.getValue());
      }

      send(dst, new Reply<K,V>(cmd.getKey(),value));
      
      return null;
    }
    
    
  }

    private String name;
    private Strategy strategy;
    private Map<K,V> data;
    private CommandFactory<K,V> factory;
    private ExecutorService workers;

    public StoreImpl(String name) {
        this.name = name;
        this.data = new HashMap<>();
  
    }

    public void init() throws Exception{
      this.workers = Executors.newCachedThreadPool();
    }

  
    public void viewAccepted(View view){
      this.strategy = new ConsistentHash(view); 
    } 

    @Override
    public V get(K k) {
      try {
        data.get(k);
      } catch(Exception e){
        return null;
      }
      
      return data.get(k);
    }

    @Override
    public V put(K k, V v) {
      V old = this.get(k);
      try {
        this.data.put(k, v);
        System.out.println("map ok");
      } catch (Exception e) {
        return null;
      }
      System.out.println("Tien bg : "+ this.data.get(k));
      return old;
    }

    @Override
    public String toString(){
        return "Store#"+name+"{"+data.toString()+"}";
    }

    public void send(Address dst, Command<K,V> command) {
      Message msg = new Message(dst,command);
    }
    public void receive(Message msg){
     
      Command cmd = (Command<K,V>) msg.getObject();
      workers.submit(new CmdHandler( msg.getDest(), cmd));
    }
    

}
