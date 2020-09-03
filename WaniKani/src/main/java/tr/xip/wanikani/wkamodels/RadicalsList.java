package tr.xip.wanikani.wkamodels;

public class RadicalsList extends ItemsList {

    @Override
    protected BaseItem.ItemType getType() {
        return BaseItem.ItemType.RADICAL;
    }
}
