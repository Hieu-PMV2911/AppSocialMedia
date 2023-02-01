package com.example.doanchuyennganh.models;

public class VCModel {
    String key, response;

    public VCModel() {
    }

    public VCModel(String key, String response) {
        this.key = key;
        this.response = response;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getResponse(String response) {
        return this.response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}
