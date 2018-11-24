this.$onInit = () => {
    this.GlobalSearchableEntityType = GlobalSearchableEntityType;
    //identfy this part, the path should be
    //JS_REFERENCE_EXPRESSION, PSI_ELEMENT_IDENTIFIER, NAME_EQ("$scope")
    //or
    //JS_REFERENCE_EXPRESSION, CHILD(PSI_ELEMENT_IDENTIFIER, NAME_EQ("$scope"))
    //or
    //JS_REFERENCE_EXPRESSION, JS_REFERENCE_EXPRESSION, CHILD(PSI_ELEMENT_IDENTIFIER, NAME_EQ("$scope"))
    $scope.hello = true;
}