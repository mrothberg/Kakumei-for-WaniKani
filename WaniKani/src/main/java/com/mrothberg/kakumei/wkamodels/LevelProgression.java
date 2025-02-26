package com.mrothberg.kakumei.wkamodels;

import androidx.annotation.NonNull;

import java.io.Serializable;

import com.mrothberg.kakumei.database.DatabaseManager;

public class LevelProgression implements Serializable, Storable {
    public int id;
    public int radicals_progress;
    public int radicals_total;
    public int kanji_progress;
    public int kanji_total;

    public LevelProgression(int id, int radicalsProgress, int radicalsTotal, int kanjiProgress, int kanjiTotal) {
        this.id = id;
        this.radicals_progress = radicalsProgress;
        this.radicals_total = radicalsTotal;
        this.kanji_progress = kanjiProgress;
        this.kanji_total = kanjiTotal;
    }

    public int getRadicalsPercentage() {
        return (int) ((double) radicals_progress / radicals_total * 100);
    }

    public int getKanjiPercentage() {
        return (int) ((double) kanji_progress / kanji_total * 100);
    }

    @Override
    public void save() {
        DatabaseManager.saveLevelProgression(this);
    }

    @NonNull
    @Override
    public String toString() {
        return "Radical Progress: " + radicals_progress + " Radical Total: " + radicals_total + " Kanji Progress: " + kanji_progress + " Kanji Total: " + kanji_total;
    }
}
