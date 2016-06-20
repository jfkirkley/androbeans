{
    "layout": "flow_layout",
    "processor": "FlowActivity",
    "fragmentContainer": "fragmentContainer",
    "startNav": {
        "target": "s1"
    },

    "steps":
    {
        "s1":        
        {
            "layout": "flow_step1_layout",

            "parentContainer": "fragmentContainer",

            "processor": "StepFragment",
            "meta": {
                "m1": 129
            },
            "data": {
                "d1": 3129
            },
            "ui": {
                "listSpec": {
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
            },

            "navMap": {
                "s2": {
                    "compName": "buttonOne",
                    "event": "onClick"
                },
                "s4": {
                    "compName": "button4",
                    "event": "onClick"
                },
                "s5": {
                    "compName": "button5",
                    "event": "onClick"
                },
                "s3": {
                    "compName": "buttonTwo",
                    "event": "onClick"
                }
            }
        },
        "s2":        
        {
            "layout": "flow_step2_layout",
            "processor": "StepFragment",
            "parentContainer": "bottomContainer",
            "meta": {
                "m1": 129
            },
            "data": {
                "d1": 3129
            },
            "navMap": {
                "s1": {
                    "compName": "button",
                    "event": "onClick"
                }
            }
        },
        "s4":        
        {
            "layout": "flow_step2_layout",
            "processor": "StepFragment",
            "parentContainer": "fragmentContainer",
            "meta": {
                "m1": 129
            },
            "data": {
                "d1": 3129
            },
            "navMap": {
                "s1": {
                    "compName": "button",
                    "event": "onClick"
                }
            }
        },
        "s5":        
        {
            "layout": "flow_step2_layout",
            "processor": "StepFragment",
            "parentContainer": "bottomContainer",
            "meta": {
                "m1": 129
            },
            "data": {
                "d1": 3129
            },
            "navMap": {
                "s1": {
                    "compName": "button",
                    "event": "onClick"
                }
            }
        },
        "s3":        
        {
            "layout": "flow_step3_layout",
            "processor": "StepFragment",
            "parentContainer": "fragmentContainer",

            "meta": {
                "m1": 129
            },
            "data": {
                "d1": 3129
            },
            "navMap": {
                "s1": {
                    "compName": "button",
                    "event": "onClick"
                }
            }
        }
    }

};
