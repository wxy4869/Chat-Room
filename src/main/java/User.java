import com.alibaba.fastjson.annotation.JSONField;

public class User {
    @JSONField(name = "id", ordinal = 1)
    private int id;
    @JSONField(name = "name", ordinal = 1)
    private String name;
    @JSONField(name = "pwd", ordinal = 1)
    private String pwd;
    private boolean active;

    public User(int id, String name, String pwd, boolean active) {
        this.id = id;
        this.name = name;
        this.pwd = pwd;
        this.active = active;
    }

    @Override
    public String toString() {
        String str = active ? "活跃" : "离线";
        return String.format("用户 id: %06d, 用户名: %s, 状态: %s", id, name, str);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
