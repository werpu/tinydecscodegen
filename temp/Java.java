class Java {

    private int prop1 = 0;
    private String prop2 = "hello world";
    private Date prop3 = new Date();
    private String prop4 = "hello 2";


    public Java(int prop1) {
        this.prop1 = prop1;
    }

    public Java(int prop1, String prop2) {
        this.prop1 = prop1;
        this.prop2 = prop2;
    }

    public Java(int prop1, String prop2, Date prop3) {
        this.prop1 = prop1;
        this.prop2 = prop2;
        this.prop3 = prop3;
    }

    public Java(int prop1, String prop2, Date prop3, String prop4) {
        this.prop1 = prop1;
        this.prop2 = prop2;
        this.prop3 = prop3;
        this.prop4 = prop4;
    }

    public int getProp1() {
        return prop1;
    }

    public void setProp1(int prop1) {
        this.prop1 = prop1;
    }

    public String getProp2() {
        return prop2;
    }

    public void setProp2(String prop2) {
        this.prop2 = prop2;
    }

    public Date getProp3() {
        return prop3;
    }

    public void setProp3(Date prop3) {
        this.prop3 = prop3;
    }

    public String getProp4() {
        return prop4+"hello world";
    }

    public void setProp4(String prop4) {
        this.prop4 = prop4;
    }
}