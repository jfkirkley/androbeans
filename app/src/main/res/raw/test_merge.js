   {
        "layout": "flow_pager_test",
        "processor": "FlowActivity",
        "fragmentContainer": "fragmentContainer",

        "startNav": {
            "useStepGenerator": true,
            "anim_in": "slide_in_left",
            "anim_out": "slide_out_right"
        },
        "__refmap__globalNavConfigs": {
            "type": "org.androware.androbeans.beans.Step",
            "map": {
                "other":        
                {

                    "parentContainer": "fragmentContainer",
                    "processor": "StepFragment",

                    "data": {
                        "number_view": "numberView"
                    },

                    "navMap": {

                        "swipeRight": {
                            "useStepGenerator": true,
                            "event": "onSwipeRight",
                            "anim_out": "slide_out_right"
                        },

                        "swipeLeft": {
                            "useStepGenerator": true,
                            "target": "__NEXT__",
                            "anim_in": "slide_in_right",
                            "anim_out": "slide_out_left"
                        }
                    }
                }
            }
        },

        "steps":
        {
            "my":        
            {
                "__merge__": "globalNavConfigs:other",

                "layout": "large_number_display",
                "processor": "StepFragment",

                "data": {
                    "text_view": "wordTextView"
                },

                "navMap": {

                    "swipeRight": {
                        "useStepGenerator": true,
                        "compName": "__ROOT_VIEW__",
                        "event": "onSwipeRight",
                        "target": "__PREV__",
                        "anim_in": "slide_in_left"
                    },
                    "swipeLeft": {
                        "useStepGenerator": true,
                        "compName": "__ROOT_VIEW__",
                        "event": "onSwipeLeft",
                        "target": "__NEXT__"
                    }
                }
            }
        }

    };
