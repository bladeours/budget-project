package com.budget.project.filter.model;

import jakarta.persistence.criteria.*;
import java.util.*;

public record Filter(
        Set<StringExpression> stringFilters,
        Set<DoubleExpression> doubleFilters,
        Set<DateExpression> dateFilters,
        LogicOperator logicOperator,
        Set<Filter> subFilters) {}
