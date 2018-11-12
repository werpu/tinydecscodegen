import angular = require('angular');
import {FirstPageController} from "./controllers/firstPage";
import {ApplicationsTable} from "./components/applicationsTable";
import {ApplicationChart} from "./components/applicationsChart";
import {BlaService} from "./services/blaService";
import {ApplicationsFilter} from "./shared/applicationsFilter";
import {CustomNavbar} from "./components/entryCustomNavbar";
import {ApplicationDetailsBox} from "./components/applicationDetailsBox";
import {ProbeComponent} from "./probeComponent";
export var entry = angular.module('app.entry', ["bla","bla1"])
    .component("mnProbeComponent", new ProbeComponent())
    .component("remappedsChart", new ApplicationChart())
    .component("remappedCustomNavbar", new CustomNavbar())
    .component("remappedDetailsBox", new ApplicationDetailsBox())
    .service("BlaService",BlaService)
    .controller("FirstPageController",FirstPageController)
    .filter("applicationOverviewsFilter", ApplicationsFilter);

