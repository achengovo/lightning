package com.achengovo.lightning.commons.message;

import java.io.Serializable;

/**
 * 响应消息
 */
public class RpcResponse implements Serializable {
    // 响应ID
    private Long id;
    // 响应结果
    private Object result;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "Response{" +
                "id='" + id + '\'' +
                ", result=" + result +
                '}';
    }
}
