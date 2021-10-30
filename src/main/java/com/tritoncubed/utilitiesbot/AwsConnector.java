package com.tritoncubed.utilitiesbot;

import software.amazon.awssdk.services.ebs.EbsClient;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.Reservation;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.List;
import java.util.stream.Collectors;

public class AwsConnector {

    private static S3Client s3Client;
    private static Ec2Client ec2Client;
    private static IamClient iamClient;
    private static EbsClient ebsClient;

    public static void setup() {
        s3Client = S3Client.builder().build();
        ec2Client = Ec2Client.builder().build();
        iamClient = IamClient.builder().build();
        ebsClient = EbsClient.builder().build();
    }

    public static List<Instance> AwsEbsList() {
        DescribeInstancesRequest request = DescribeInstancesRequest.builder()
                .maxResults(20)
                .build();
        DescribeInstancesResponse response = ec2Client.describeInstances(request);

        return response.reservations().stream()
                .map(Reservation::instances)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    public static Instance AwsEbsStatus(String id) {
        DescribeInstancesRequest request = DescribeInstancesRequest.builder()
                .instanceIds(id)
                .build();
        DescribeInstancesResponse response = ec2Client.describeInstances(request);

        return response.reservations().stream()
                .map(Reservation::instances)
                .flatMap(List::stream)
                .findFirst()
                .orElse(null);
    }
}
