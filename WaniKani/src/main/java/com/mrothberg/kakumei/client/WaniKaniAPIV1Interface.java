package com.mrothberg.kakumei.client;

import java.io.Serializable;

import com.mrothberg.kakumei.wkamodels.KanjiList;
import com.mrothberg.kakumei.wkamodels.LevelProgression;
import com.mrothberg.kakumei.wkamodels.RadicalsList;
import com.mrothberg.kakumei.wkamodels.SRSDistribution;
import com.mrothberg.kakumei.wkamodels.StudyQueue;
import com.mrothberg.kakumei.wkamodels.VocabularyList;

import java9.util.concurrent.CompletableFuture;


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