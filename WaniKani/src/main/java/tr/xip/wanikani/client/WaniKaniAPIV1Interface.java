package tr.xip.wanikani.client;

import java.io.Serializable;
import java.util.concurrent.CompletableFuture;

import tr.xip.wanikani.wkamodels.KanjiList;
import tr.xip.wanikani.wkamodels.LevelProgression;
import tr.xip.wanikani.wkamodels.RadicalsList;
import tr.xip.wanikani.wkamodels.SRSDistribution;
import tr.xip.wanikani.wkamodels.StudyQueue;
import tr.xip.wanikani.wkamodels.VocabularyList;

public interface WaniKaniAPIV1Interface extends Serializable {
    String RADICAL = "radical";
    String KANJI = "kanji";
    String VOCABULARY = "vocabulary";

    CompletableFuture<StudyQueue> getStudyQueue();

    CompletableFuture<LevelProgression> getCurrentLevelProgression();

    CompletableFuture<SRSDistribution> getSRSDistribution();

    CompletableFuture<KanjiList> getKanjiList(String level);

    CompletableFuture<RadicalsList> getRadicalsList(String level);

    CompletableFuture<VocabularyList> getVocabularyList(String level);
}