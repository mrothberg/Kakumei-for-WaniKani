package tr.xip.wanikani.wkamodels;

public class VocabularyList extends ItemsList {

    @Override
    protected BaseItem.ItemType getType() {
        return BaseItem.ItemType.VOCABULARY;
    }
}
