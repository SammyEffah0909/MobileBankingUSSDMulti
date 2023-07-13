/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobile.bank.ussd.model;

import java.io.Serializable;

/**
 *
 * @author Seth Sebeh-Kusi
 */
public class Request implements Serializable{
    private static final long serialVersionUID = -239319372154451488L;
    private String id;
    private String msg;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
