package xyz.ret.base.redistimer;

public class Task {
    private String type;
    private Object param;

    public Task() {
    }

    public Task(String type, Object param) {
        this.type = type;
        this.param = param;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getParam() {
        return param;
    }

    public void setParam(Object param) {
        this.param = param;
    }
}
