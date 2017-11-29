package com.project.context.iparking.com.prolificinteractive.materialcalendarview;

class DecoratorResult {
    public final DayViewDecorator decorator;
    public final DayViewFacade result;

    DecoratorResult(DayViewDecorator decorator, DayViewFacade result) {
        this.decorator = decorator;
        this.result = result;
    }
}
