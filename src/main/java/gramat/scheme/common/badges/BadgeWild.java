package gramat.scheme.common.badges;

public class BadgeWild extends Badge {

    @Override
    public boolean isWild() {
        return true;
    }

    @Override
    public String toString() {
        return "*";
    }
}