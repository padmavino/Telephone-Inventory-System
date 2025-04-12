package com.telecom.inventory.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "#{@environment.getProperty('app.elasticsearch.index-name')}")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TelephoneNumberDocument {

    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String number;

    @Field(type = FieldType.Keyword)
    private String countryCode;

    @Field(type = FieldType.Keyword)
    private String areaCode;

    @Field(type = FieldType.Keyword)
    private String numberType;

    @Field(type = FieldType.Keyword)
    private String category;

    @Field(type = FieldType.Text)
    private String features;

    @Field(type = FieldType.Keyword)
    private String status;
}
