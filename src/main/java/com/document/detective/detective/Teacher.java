package com.document.detective.detective;

import annotations.ClassDocument;
import annotations.MethodDocument;

@ClassDocument
public class Teacher {
    private String name;
    @MethodDocument
    public String getName(){
        return name;
    }
}
