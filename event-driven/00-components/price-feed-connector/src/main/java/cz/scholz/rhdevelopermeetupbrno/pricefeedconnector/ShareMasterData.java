package cz.scholz.rhdevelopermeetupbrno.pricefeedconnector;

public class ShareMasterData {
    private String code;
    private float price;
    private int maxChange;

    public ShareMasterData(String code, float price, int maxChange) {
        this.code = code;
        this.price = price;
        this.maxChange = maxChange;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public int getMaxChange() {
        return maxChange;
    }

    public void setMaxChange(int maxChange) {
        this.maxChange = maxChange;
    }
}
