package com.guilhermegabriel.projetopdm;

public enum SortCriterion {
    BY_TITLE_ASC("Título (A-Z)"),
    BY_TITLE_DESC("Título (Z-A)"),
    BY_PLATFORM_ASC("Plataforma"),
    BY_PROGRESS_DESC("Progresso (Maior primeiro)"),
    BY_DATE_ADDED_DESC("Mais Recentes"),
    BY_RATING_DESC("Nota (Maior primeiro)");

    private final String friendlyName;

    SortCriterion(String name) {
        this.friendlyName = name;
    }

    @Override
    public String toString() {
        return friendlyName;
    }
}
