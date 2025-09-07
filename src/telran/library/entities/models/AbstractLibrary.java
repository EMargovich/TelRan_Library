package telran.library.entities.models;

public abstract class AbstractLibrary implements ILibrary {

protected int maxPicPeriod;
protected int minPickPeriod;

    public AbstractLibrary() {
        maxPicPeriod = 30;
        minPickPeriod = 3;
    }

    public int getMaxPicPeriod() {
        return maxPicPeriod;
    }

    public void setMaxPicPeriod(int maxPicPeriod) {
        if(maxPicPeriod > 0) this.maxPicPeriod = maxPicPeriod;
    }

    public int getMinPickPeriod() {
        return minPickPeriod;
    }

    public void setMinPickPeriod(int minPickPeriod) {
        if(minPickPeriod > 0) this.minPickPeriod = minPickPeriod;
    }
}
