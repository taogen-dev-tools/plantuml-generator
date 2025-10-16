@startuml

<#list entities as myEntity>

'entity
${myEntity.type} ${myEntity.className}
<#if myEntity.type != "annotation">
{
<#if commandOptions.membersDisplayed ?? && commandOptions.membersDisplayed == true>
    'fields
    <#if myEntity.fields??>
    <#list myEntity.fields as field>
    ${field.visibility!""} ${field.isStatic!""} ${field.isFinal!""} ${field.type} ${field.name}
    </#list>
    </#if>
    'methods
    <#if myEntity.methods??>
    <#list myEntity.methods as method>
    ${method.visibility!""} ${method.isStatic!""} ${method.isAbstract!""} ${method.returnType!""} ${method.name}(${method.params!""})
    </#list>
    </#if>
</#if>
}
</#if>

'relationship
<#if myEntity.parentClass??>
${myEntity.parentClass} <|-- ${myEntity.classNameWithoutGeneric}
</#if>
<#if myEntity.parentInterfaces??>
<#list myEntity.parentInterfaces as interfaceName>
<#if myEntity.type == "interface">
${interfaceName} <|-- ${myEntity.classNameWithoutGeneric}
<#else>
${interfaceName} <|.. ${myEntity.classNameWithoutGeneric}
</#if>
</#list>
</#if>

</#list>

@enduml
