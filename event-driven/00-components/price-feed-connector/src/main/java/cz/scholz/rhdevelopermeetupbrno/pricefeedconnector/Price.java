package cz.scholz.rhdevelopermeetupbrno.pricefeedconnector;

public class Price {
    private String share;
    private Float price;

    public Price(String share, Float price) {
        this.share = share;
        this.price = price;
    }

    public String getShare() {
        return share;
    }

    public void setShare(String share) {
        this.share = share;
    }

    public Float getPrice() {
        return price;
    }

    public void setPrice(Float price) {
        this.price = price;
    }
}
