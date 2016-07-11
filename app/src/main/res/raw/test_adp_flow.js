{
    "layout": "lesson_flow_layout",
    "processor": "FlowActivity",
    "fragmentContainer": "lessonListContainer",

    "startNav": {
        "target": "lessonCategories",
        "anim_in": "slide_in_left",
        "anim_out": "slide_out_right"
    },

    "__refmap__ListItemTemplates": {
        "type": "java.util.HashMap",
        "map": {
            "alphabet":
            { 
                "target": "startLesson",
                "labelFieldId": "labelField",
                "props": {
                    "lessonId": "lesson_alphabet",
                    "layout": "single_letter_display",
                    "viewCustomizerSpec": {
                        "targetClassName": "biz.engezy.www.engezy.lesson.SimpleTextViewCustomizer"
                    }
                }
            },
            "numbers":
            { 
                "target": "startLesson",
                "labelFieldId": "labelField",
                "props": {
                    "lessonId": "lesson_numbers",
                    "layout": "large_number_display",
                    "resultsComparator": "biz.engezy.www.engezy.lesson.NumericResultComparator",
                    "viewCustomizerSpec": {
                        "targetClassName": "biz.engezy.www.engezy.lesson.NumberLessonViewCustomizer"
                    }
                }
            },
            "nouns":
            { 
                "target": "startLesson",
                "labelFieldId": "labelField",
                "props": {
                    "lessonId": "lesson_nouns",
                    "layout": "word_picture_display",
                    "viewCustomizerSpec": {
                        "targetClassName": "biz.engezy.www.engezy.lesson.ImageLessonViewCustomizer"
                    }
                }
            }

        }
    },


    "steps":
    {
        "lessonCategories":        
        {
            "layout": "lesson_catlist_layout",
            "parentContainer": "lessonListContainer",
            "processor": "org.androware.flow.StepFragment",
            "ui": {
                "adapterViews": {
                    "lessonList": {
                        "adapterConstructorSpec": {
                            "targetClassName": "biz.engezy.www.engezy.lesson.LessonListAdapter",
                            "paramClassNames": [  "android.app.Activity", "int", "java.util.List"],
                            "paramObjects": ["__plugin__context", "R.layout.lesson_catitem_layout", 
                                             "__plugin__items" ]
                        },
                        "viewId": "lessonCategoryList",
                        "itemLayoutId": "lesson_catitem_layout",
                        "items": [
                            { 
                                "label": "Alphabet",
                                "target": "alphabet",
                                "labelFieldId": "labelField"
                            },
                            {
                                "label": "Numbers",
                                "target": "numbers",
                                "labelFieldId": "labelField"

                            },
                            {
                                "label": "Nouns",
                                "target": "nouns",
                                "labelFieldId": "labelField"
                            },

                            {
                                "label": "Pronouns",
                                "target": "pronouns",
                                "labelFieldId": "labelField"
                            },
                            {
                                "label": "Family Relations",
                                "target": "family",
                                "labelFieldId": "labelField"
                            }
                        ]
                    }
                }
            },
            "data": {
            },
            "navMap": {
                "lessonList": {
                    "compName": "lessonCategoryList",
                    "event": "onItemClick"
                }
            }
        },

        "alphabet":        
        {
            "layout": "lesson_catlist_layout",
            "parentContainer": "lessonListContainer",
            "processor": "org.androware.flow.StepFragment",
            "meta": {
            },
            "ui": {
                "adapterViews": {
                    "lessonList": {
                        "viewId": "lessonCategoryList",
                        "itemLayoutId": "lesson_catitem_layout",
                        "adapterConstructorSpec": {
                            "targetClassName": "biz.engezy.www.engezy.lesson.LessonListAdapter",
                            "paramClassNames": [  "android.app.Activity", "int", "java.util.List"],
                            "paramObjects": ["__plugin__context", "R.layout.lesson_catitem_layout", 
                                             "__plugin__items" ]
                        },
                        "items": [
                            { 
                                "__merge__": "ListItemTemplates:alphabet",
                                "label": "Complete Alphabet",
                                "props": {
                                    "studyIndex": 0
                                }
                            },
                            {
                                "__merge__": "ListItemTemplates:alphabet",
                                "label": "Vowels",
                                "props": {
                                    "studyIndex": 1
                                }
                            },
                            {
                                "__merge__": "ListItemTemplates:alphabet",
                                "label": "Consonants",
                                "props": {
                                    "studyIndex": 2

                                }
                            }
                        ]
                    }
                }
            },
            "navMap": {
                "lessonList": {
                    "compName": "lessonCategoryList",
                    "event": "onItemClick"
                }
            }

        },

        "numbers":        
        {
            "layout": "lesson_catlist_layout",
            "parentContainer": "lessonListContainer",
            "processor": "org.androware.flow.StepFragment",
            "meta": {
            },
            "ui": {
                "adapterViews": {
                    "lessonList": {
                        "viewId": "lessonCategoryList",
                        "itemLayoutId": "lesson_catitem_layout",
                        "adapterConstructorSpec": {
                            "targetClassName": "biz.engezy.www.engezy.lesson.LessonListAdapter",
                            "paramClassNames": [  "android.app.Activity", "int", "java.util.List"],
                            "paramObjects": ["__plugin__context", "R.layout.lesson_catitem_layout", 
                                             "__plugin__items" ]
                        },
                        "items": [
                            { 
                                "__merge__": "ListItemTemplates:numbers",
                                "label": "1 to 10",
                                "props": {
                                    "studyIndex": 0
                                }
                            },
                            {
                                "__merge__": "ListItemTemplates:numbers",
                                "label": "10 to 20",
                                "props": {
                                    "studyIndex": 1
                                }
                            },
                            {
                                "__merge__": "ListItemTemplates:numbers",
                                "label": "10 to 100",
                                "props": {
                                    "studyIndex": 2
                                }
                            },
                            {
                                "__merge__": "ListItemTemplates:numbers",
                                "label": "100 to 1000",
                                "props": {
                                    "studyIndex": 3
                                }
                            }
                        ]
                    }
                }
            },
            "navMap": {
                "lessonList": {
                    "compName": "lessonCategoryList",
                    "event": "onItemClick"
                }
            }
        },

        "nouns":        
        {
            "layout": "lesson_catlist_layout",
            "parentContainer": "lessonListContainer",
            "processor": "org.androware.flow.StepFragment",
            "meta": {
            },
            "ui": {
                "adapterViews": {
                    "lessonList": {
                        "viewId": "lessonCategoryList",
                        "itemLayoutId": "lesson_catitem_layout",
                        "adapterConstructorSpec": {
                            "targetClassName": "biz.engezy.www.engezy.lesson.LessonListAdapter",
                            "paramClassNames": [  "android.app.Activity", "int", "java.util.List"],
                            "paramObjects": ["__plugin__context", "R.layout.lesson_catitem_layout", 
                                             "__plugin__items" ]
                        },
                        "items": [
                            { 
                                "__merge__": "ListItemTemplates:nouns",
                                "label": "Animals",
                                "props": {
                                    "studyIndex": 0
                                }
                            }
                        ]
                    }
                }
            },
            "navMap": {
                "lessonList": {
                    "compName": "lessonCategoryList",
                    "event": "onItemClick"
                }
            }
        },


        "startLesson":        
        {
            "layout": "start_lesson_layout",
            "parentContainer": "lessonListContainer",
            "processor": "org.androware.flow.StepFragment",
            "transitionClassName": "biz.engezy.www.engezy.lesson.LessonActivityStepTransition",
            "meta": {
            },
            "ui": {
            },
            "data": {
            },
            "navMap": {
                "listen": {
                    "compName": "listenButton",
                    "event": "onClick"
                },
                "practice": {
                    "compName": "practiceButton",
                    "event": "onClick"
                },
                "test": {
                    "compName": "testButton",
                    "event": "onClick"
                },
                "help": {
                    "compName": "helpButton",
                    "event": "onClick"
                }
            }
        },

        "listen":        
        {
            "processor": "biz.engezy.www.engezy.lesson.LessonFlowPagerActivity",
            "layout":  "new_activity_playback",
            "targetFlow": "lesson_text_pager",
            "data": {
                "mode": "listen"
            }
        },
        "practice":        
        {
            "processor": "biz.engezy.www.engezy.lesson.LessonFlowPagerActivity",
            "layout":  "new_activity_playback",
            "targetFlow": "lesson_text_pager",
            "data": {
                "mode": "practice"
            }
        },
        "test":        
        {
            "processor": "biz.engezy.www.engezy.lesson.LessonFlowPagerActivity",
            "layout":  "new_activity_playback",
            "targetFlow": "lesson_text_pager",
            "data": {
                "mode": "test"
            }
        },
        "help":        
        {
            "processor": "biz.engezy.www.engezy.NewPagerActivity",
            "data": {
                "mode": "help"
            }
        },
        "alphabetold":        
        {
            "processor": "biz.engezy.www.engezy.lesson.LessonActivity",

            "transitionClassName": "biz.engezy.www.engezy.lesson.LessonActivityStepTransition",
            "meta": {
            },
            "data": {
            }
        },
        "testSR":        
        {
            "processor": "biz.engezy.www.engezy.TestSRActivity",
            "transitionClassName": "biz.engezy.www.engezy.lesson.LessonActivityStepTransition",
            "meta": {
            },
            "data": {
            }
        },
        "talk":        
        {
            "processor": "biz.engezy.www.engezy.talk.TalkActivity",
            "transitionClassName": "biz.engezy.www.engezy.lesson.LessonActivityStepTransition",
            "meta": {
            },
            "data": {
                "biz.engezy.www.engezy.LessonPrefix": "lesson_0002"
            }
        }

    }
};
