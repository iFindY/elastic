package de.arkadi.elasticsearch.twitter;

import de.arkadi.elasticsearch.model.SaveDTO;

import java.util.ArrayList;
import java.util.List;

public class MessageStorageTesting {

    private List<SaveDTO> storage = new ArrayList<>();

    public void count(){

        System.out.println(storage.size());
    }

    public void put(SaveDTO message) {
        storage.add( message );
        System.out.println(message.toString());
    }

    public String toString() {
        StringBuffer info = new StringBuffer();
        storage.forEach( msg -> info.append( msg ).append( "<br/>" ) );
        return info.toString();
    }

    public void clear() {
        storage.clear();
    }
}
