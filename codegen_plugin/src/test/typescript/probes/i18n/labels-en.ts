export const language = {
    model: {
        pamApplication: {
            name: 'Application',
            key: {
                ictoNumber: 'ICTO Number'
            }
        },
        asset: {
            name: 'Asset',
            combinedStatus: 'Combined Status',
            status: {
                assetStatusApprovalsUndefined: '',
                assetStatusApprovalsNew: 'New',
                assetStatusApprovalsPending: 'Approval pending',
                assetStatusApprovalsRejected: 'REJECTED',
                assetStatusApprovalsCancelled: 'Cancelled by user/system',
                assetStatusProvisioningAwaited: 'Awaited',
                assetStatusProvisioningInProgress: 'Provisioning in progress',
                assetStatusProvisioningError: 'Error in provisioning',
                assetStatusProvisioningActive: 'Activated',
                assetStatusProvisioningExpired: 'Expired',
            }
        },
        assetGroup: {
            name: 'Name',
            groupingRuleAttribute: {
                platform: 'Platform',
                environment: 'Environment',
                region: 'Region'
            },
            groupingRuleValueEmpty: 'ALL'
        },
        platform: {
            key: {
                key: 'Platform'
            }
        },
        stage: {
            key: {
                key: 'Stage'
            }
        },
        applicationFunction: {
            ITAO: 'ITAO',
            0: 'ITAO',
            ITAO_DEPUTY: 'Deputy',
            1: 'Deputy',
            BAO: 'BAO',
            2: 'BAO',
            PSL: 'PSL',
            3: 'PSL'
        },
        activationRequestStatus: {
            '_title': 'Request Status',
            NEW: 'New',
            '0': 'New',
            APPROVALS_PENDING: 'Approval pending',
            '1': 'Approval pending',
            APPROVED: 'Approved',
            '2': 'Approved',
            DEPLOYED: 'Deployed',
            '3': 'Deployed',
            CANCELLED: 'Cancelled',
            '4': 'Cancelled',
            REJECTED: 'Rejected',
            '5': 'Rejected',
            DEACTIVATED: 'Deactivated',
            '6': 'Deactivated'
        },
        changeType: {
            NEW: 'Added',
            MODIFY: 'Modified',
            DELETE: 'Removed',
            NONE: 'Unchanged'
        },
        definitionRequestStatus: {
            undefined: '-',
            NEW: 'Draft',
            '0': 'Draft',
            APPROVALS_PENDING: 'Pending approval',
            '1': 'Pending approval',
            APPROVED: 'Approved',
            '2': 'Approved',
            WAITING_FOR_SECURITY_LOGIC: 'Waiting for security logic',
            '3': 'Waiting for security logic',
            READY_TO_DEPLOY: 'Ready to deploy',
            '4': 'Ready to deploy',
            DEPLOYED: 'Onboarded',
            '5': 'Onboarded',
            DECLINED: 'Rejected',
            '6': 'Rejected',
            CANCELLED: 'Cancelled',
            '7': 'Cancelled'
        },
        definitionOrderStatus: {
            undefined: '-',
            DRAFT: 'Draft',
            '0': 'Draft',
            SUBMITTED: 'Submitted',
            '1': 'Submitted',
            REQUEST_PROCESSING_FINISHED: 'Requests processed',
            '2': 'Requests processed',
            WAITING_FOR_RIM: 'Waiting for RIM',
            '3': 'Waiting for RIM',
            READY_TO_DEPLOY: 'Ready to deploy',
            '4': 'Ready to deploy',
            DEPLOYED: 'Done',
            '5': 'Done'
        },
        externalDataSource: {
            AURA: 'Aura',
            '0': 'Aura',
            CATI: 'Cati',
            '1': 'Cati',
            SNOW: 'Snow',
            '2': 'Snow',
            TEST: 'Test',
            '3': 'Test'
        },
        externalDataSourceDetails: {
            AURA: 'Aura',
            CATI: 'Cati',
            SNOW: 'Snow',
            TEST: 'Test'
        },
        activationRequestStatusGroup: {
            CURRENT: 'Current',
            '0': 'Current',
            FAILED: 'Failed',
            '1': 'Failed',
            HISTORIC: 'History',
            '2': 'History'
        },
        cancellationReason: {
            _title: 'Request Status',
            CANCELLED_BY_USER: 'Manually cancelled by user',
            '0': 'Manually cancelled by user',
            NOT_APPROVED_WITHIN_LIMIT: 'Not approved within a specific period',
            '1': 'Not approved within a specific period',
        },
        privilegedAccessEntitlementStatus: {
            _title: 'Distribution Status',
            PROVISIONING_IN_PROGRESS: 'In progress',
            '0': 'In progress',
            ACTIVE: 'Active',
            '1': 'Active',
            PROVISIONING_ERROR: 'Error',
            '2': 'Error',
            EXPIRED: 'Expired',
            '3': 'Expired',
        },
        approvalStatus: {
            WAITING_FOR_APPROVAL: 'Pending approval',
            '0': 'Pending approval',
            APPROVED: 'Approved',
            '1': 'Approved',
            REJECTED: 'Rejected',
            '2': 'Rejected',
            OBSOLETE: 'Obsolete',
            '3': 'Obsolete'
        },
        activationRequest: {
            key: {
                requestId: 'Request Id'
            },
            requester: 'Requester',
            application: 'Application',
            approvalMode: 'Approval Type',
            waitForApprovalUntil: 'Due Date/Time',
            auditInfo: {
                createdAt: 'Creation Date/Time'
            }
        },
        definitionRequest: {
            key: {
                requestId: 'Request Id'
            },
            requester: 'Requester',
            application: 'Application',
            auditInfo: {
                createdAt: 'Creation Date/Time'
            }
        },
        approvalItem: {
            type: 'Approval Type',
            status: 'Status'
        },
        approvalMode: {
            undefined: 'Undefined',
            APPROVAL: 'Pre Approval',
            '0': 'Pre Approval',
            REVIEW: 'Post Approval',
            '1': 'Post Approval'
        },

        jobStatus: {
            WAITING: 'Waiting',
            SCHEDULED: 'Scheduled',
            PENDING: 'Pending',
            STOPPING: 'Stopping',
            HALTED: 'Halted',
            PROCESSING: 'Processing',
            SUCCESS: 'Success',
            SUCCESS_WITH_WARNING: 'Attention',
            ERROR: 'Error',
            NOT_SUCCESSFUL: '(Still) not Successful'
        },

        taskType: {
            ACTIVATION: 'Activation Approval',
            ONBOARDING: 'TPA Onboarding Approval'
        }
    },
    shared: {
        unknownFailure: 'Unknown failure.',
        validDate: (format: String) => `Date format ${format}`,
        fromDate: (format: String) => `Date from ${format}`,
        toDate: (format: String) => `Date to ${format}`,
        fromValid: (format: String) => `Valid from ${format}`,
        action: 'Action',
    },
    onboarding: {
        header: 'ICH BIN EIN HEADER',
        footer: 'def',
        request: (requestId: String) => `Request with id: ${requestId}`,
        externalSourceDescription: (role: string,
                                    source: string) => `The role '${role}' has been sourced from '${source}'.`,
        externalSourceDescriptionNone: 'There are no roles assigned to you for this application.'
    },
    messaging: {
        messageSent: 'Message has been sent.'
    },

    activationRequestExtension: {
        extendSuccess: 'Activation request successfully extended.'
    }
};