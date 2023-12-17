package com.document.detective.detective;

import annotations.ClassDocument;
import annotations.MethodDocument;

@ClassDocument
public class College {
    /**
     * This Method only return a static value WB
     */
    @MethodDocument
    public String getCollageSate(){
        return "WB";
    }
}
