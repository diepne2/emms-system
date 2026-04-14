package com.emms.backend.advancedsearch;


import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public enum SearchOperation {

    CONTAINS("cn"),
    DOES_NOT_CONTAIN("nc"),
    EQUAL("eq"),
    NOT_EQUAL("ne"),
    BEGINS_WITH("bw"),
    DOES_NOT_BEGIN_WITH("bn"),
    ENDS_WITH("ew"),
    DOES_NOT_END_WITH("en"),
    NUL("nu"),
    NOT_NULL("nn"),
    GREATER_THAN("gt"),
    GREATER_THAN_EQUAL("ge"),
    LESS_THAN("lt"),
    LESS_THAN_EQUAL("le"),
    IN("in"),
    IN_MANY_TO_MANY("inm"),
    ANY("any"),
    ALL("all");

    private final String code;

    SearchOperation(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static final Set<String> SIMPLE_OPERATION_SET = new HashSet<>(Arrays.asList(
            "cn", "nc", "eq", "ne", "bw", "bn", "ew", "en",
            "nu", "nn", "gt", "ge", "lt", "le", "in", "inm"
    ));

    public static SearchOperation getDataOption(String dataOption) {
        if (dataOption == null || dataOption.isBlank()) {
            return null;
        }

        String value = dataOption.trim().toLowerCase(Locale.ROOT);
        switch (value) {
            case "all":
                return ALL;
            case "any":
                return ANY;
            default:
                return null;
        }
    }

    public static SearchOperation getSimpleOperation(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }

        String value = input.trim().toLowerCase(Locale.ROOT);
        switch (value) {
            case "cn":
                return CONTAINS;
            case "nc":
                return DOES_NOT_CONTAIN;
            case "eq":
                return EQUAL;
            case "ne":
                return NOT_EQUAL;
            case "bw":
                return BEGINS_WITH;
            case "bn":
                return DOES_NOT_BEGIN_WITH;
            case "ew":
                return ENDS_WITH;
            case "en":
                return DOES_NOT_END_WITH;
            case "nu":
                return NUL;
            case "nn":
                return NOT_NULL;
            case "gt":
                return GREATER_THAN;
            case "ge":
                return GREATER_THAN_EQUAL;
            case "lt":
                return LESS_THAN;
            case "le":
                return LESS_THAN_EQUAL;
            case "in":
                return IN;
            case "inm":
                return IN_MANY_TO_MANY;
            default:
                return null;
        }
    }

    public static SearchOperation fromCode(String input) {
        return getSimpleOperation(input);
    }

    public static SearchOperation fromValue(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }

        String value = input.trim().toLowerCase(Locale.ROOT);

        SearchOperation byCode = getSimpleOperation(value);
        if (byCode != null) {
            return byCode;
        }

        switch (value) {
            case "contains":
                return CONTAINS;
            case "does_not_contain":
                return DOES_NOT_CONTAIN;
            case "equal":
                return EQUAL;
            case "not_equal":
                return NOT_EQUAL;
            case "begins_with":
                return BEGINS_WITH;
            case "does_not_begin_with":
                return DOES_NOT_BEGIN_WITH;
            case "ends_with":
                return ENDS_WITH;
            case "does_not_end_with":
                return DOES_NOT_END_WITH;
            case "nul":
            case "null":
                return NUL;
            case "not_null":
                return NOT_NULL;
            case "greater_than":
                return GREATER_THAN;
            case "greater_than_equal":
                return GREATER_THAN_EQUAL;
            case "less_than":
                return LESS_THAN;
            case "less_than_equal":
                return LESS_THAN_EQUAL;
            case "in":
                return IN;
            case "in_many_to_many":
                return IN_MANY_TO_MANY;
            case "any":
                return ANY;
            case "all":
                return ALL;
            default:
                return null;
        }
    }

    public static boolean isSimpleOperation(String input) {
        if (input == null || input.isBlank()) {
            return false;
        }
        return SIMPLE_OPERATION_SET.contains(input.trim().toLowerCase(Locale.ROOT));
    }
}