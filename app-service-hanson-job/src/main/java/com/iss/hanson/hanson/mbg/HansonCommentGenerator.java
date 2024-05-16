package com.iss.hanson.hanson.mbg;

import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.config.MergeConstants;
import org.mybatis.generator.config.PropertyRegistry;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import static org.mybatis.generator.internal.util.StringUtility.isTrue;


/**
 * mybatis generator 自定义comment生成器. 基于MBG 1.3.2.
 * 
 * @author huhanlin
 *
 */
public class HansonCommentGenerator implements CommentGenerator {

	private Properties properties;
	private boolean suppressDate;
	private boolean suppressAllComments;
	private String currentDateStr;

	public HansonCommentGenerator() {
		super();
		properties = new Properties();
		suppressDate = false;
		suppressAllComments = false;
		currentDateStr = (new SimpleDateFormat("yyyy-MM-dd")).format(new Date());
	}

	public void addJavaFileComment(CompilationUnit compilationUnit) {
		// add no file level comments by default
		return;
	}

	/**
	 * Adds a suitable comment to warn users that the element was generated, and
	 * when it was generated.
	 */
	public void addComment(XmlElement xmlElement) {
		return;
	}

	public void addRootComment(XmlElement rootElement) {
		// add no document level comments by default
		return;
	}

	public void addConfigurationProperties(Properties properties) {
		this.properties.putAll(properties);

		suppressDate = isTrue(properties.getProperty(PropertyRegistry.COMMENT_GENERATOR_SUPPRESS_DATE));

		suppressAllComments = isTrue(properties.getProperty(PropertyRegistry.COMMENT_GENERATOR_SUPPRESS_ALL_COMMENTS));
	}

	/**
	 * This method adds the custom javadoc tag for. You may do nothing if you do
	 * not wish to include the Javadoc tag - however, if you do not include the
	 * Javadoc tag then the Java merge capability of the eclipse plugin will
	 * break.
	 * 
	 * @param javaElement
	 *            the java element
	 */
	protected void addJavadocTag(JavaElement javaElement, boolean markAsDoNotDelete) {
		javaElement.addJavaDocLine(" *");
		StringBuilder sb = new StringBuilder();
		sb.append(" * ");
		sb.append(MergeConstants.NEW_ELEMENT_TAG);
		if (markAsDoNotDelete) {
			sb.append(" do_not_delete_during_merge");
		}
		String s = getDateString();
		if (s != null) {
			sb.append(' ');
			sb.append(s);
		}
		javaElement.addJavaDocLine(sb.toString());
	}

	/**
	 * This method returns a formated date string to include in the Javadoc tag
	 * and XML comments. You may return null if you do not want the date in
	 * these documentation elements.
	 * 
	 * @return a string representing the current timestamp, or null
	 */
	protected String getDateString() {
		String result = null;
		if (!suppressDate) {
			result = currentDateStr;
		}
		return result;
	}

	public void addClassComment(InnerClass innerClass, IntrospectedTable introspectedTable) {
		if (suppressAllComments) {
			return;
		}
		StringBuilder sb = new StringBuilder();
		innerClass.addJavaDocLine("/**");
		sb.append(" * ");
		sb.append(introspectedTable.getFullyQualifiedTable());
		sb.append(" ");
		sb.append(getDateString());
		innerClass.addJavaDocLine(sb.toString());
		innerClass.addJavaDocLine(" */");
	}

	public void addEnumComment(InnerEnum innerEnum, IntrospectedTable introspectedTable) {
		if (suppressAllComments) {
			return;
		}

		StringBuilder sb = new StringBuilder();

		innerEnum.addJavaDocLine("/**");
		// addJavadocTag(innerEnum, false);
		sb.append(" * ");
		sb.append(introspectedTable.getFullyQualifiedTable());
		innerEnum.addJavaDocLine(sb.toString());
		innerEnum.addJavaDocLine(" */");
	}

	public void addFieldComment(Field field, IntrospectedTable introspectedTable,
			IntrospectedColumn introspectedColumn) {
		if (suppressAllComments) {
			return;
		}

		StringBuilder sb = new StringBuilder();

		field.addJavaDocLine("/**");
		sb.append(" * ");
		sb.append(introspectedColumn.getRemarks());
		field.addJavaDocLine(sb.toString());

		// addJavadocTag(field, false);
		field.addJavaDocLine(" */");
		try {
			//filed type 
			String className = field.getType().getFullyQualifiedName();
			String fieldName = field.getName();
			System.out.println(className);
			//如果是主键
			//系统字段不传递，添加@JsonIgnore注解
			if("id".equals(fieldName)){
				field.addJavaDocLine("@TableId(value = \""+introspectedColumn.getActualColumnName()+"\",type = IdType.AUTO)");
				return;
			}

			//添加描述注解用于页面字段展示
			field.addJavaDocLine("@TableField(\""+introspectedColumn.getActualColumnName()+"\")");//输出的格式
			//为了不关联其他项目，所以使用了indexof可能不准确,枚举需要是enum
			if(className.indexOf("Date") >= 0 && fieldName.indexOf("createTime") < 0 && fieldName.indexOf("updateTime") < 0){
				//日期类型添加日期的注解
//				field.addJavaDocLine("@DateTimeFormat(pattern = DatePattern.ISO_DATE)");//输出的格式
//				field.addJavaDocLine("@JsonFormat(pattern = DatePattern.ISO_DATE_TIME,timezone = DatePattern.TIME_ZONE)");//输入接收的格式
			}else if(className.indexOf("enum") >= 0 && fieldName.indexOf("dataStatus") < 0){
				//枚举类型添加枚举的注解
				field.addJavaDocLine("@JsonSerialize(using = EnumJsonSerializer.class)");
			}

			//系统字段不传递，添加@JsonIgnore注解
			if(fieldName.indexOf("createTime") >= 0 || fieldName.indexOf("updateTime") >= 0 || fieldName.indexOf("createUserId") >= 0
				|| fieldName.indexOf("updateUserId") >= 0 || fieldName.indexOf("deleted") >= 0){
				field.addJavaDocLine("@JsonIgnore");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

	public void addFieldComment(Field field, IntrospectedTable introspectedTable) {
		if (suppressAllComments) {
			return;
		}
		StringBuilder sb = new StringBuilder();

		field.addJavaDocLine("/**");
		sb.append(" * ");
		sb.append(introspectedTable.getFullyQualifiedTable());
		field.addJavaDocLine(sb.toString());
		field.addJavaDocLine(" */");
	}

	public void addGeneralMethodComment(Method method, IntrospectedTable introspectedTable) {
		if (suppressAllComments) {
			return;
		}
		// method.addJavaDocLine("/**");
		// addJavadocTag(method, false);
		// method.addJavaDocLine(" */");
	}



	public void addGetterComment(Method method, IntrospectedTable introspectedTable,
			IntrospectedColumn introspectedColumn) {
		return;
	}

	public void addSetterComment(Method method, IntrospectedTable introspectedTable,
			IntrospectedColumn introspectedColumn) {
		return;
	}

	public void addClassComment(InnerClass innerClass, IntrospectedTable introspectedTable, boolean markAsDoNotDelete) {
		if (suppressAllComments) {
			return;
		}

		StringBuilder sb = new StringBuilder();

		innerClass.addJavaDocLine("/**");
		sb.append(" * ");
		sb.append(introspectedTable.getFullyQualifiedTable());
		innerClass.addJavaDocLine(sb.toString());

		sb.setLength(0);
		sb.append(" * @author ");
		sb.append("huhanlin");
		sb.append(" ");
		sb.append(currentDateStr);

		// addJavadocTag(innerClass, markAsDoNotDelete);

		innerClass.addJavaDocLine(" */");


    }

	public void addModelClassComment(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		if (suppressAllComments) {
			return;
		}

        topLevelClass.addSuperInterface(new FullyQualifiedJavaType("Serializable"));
		
		//引包
//		topLevelClass.addJavaDocLine("import org.springframework.format.annotation.DateTimeFormat;");
//		topLevelClass.addJavaDocLine("import com.fasterxml.jackson.annotation.JsonFormat;");
//		topLevelClass.addJavaDocLine("import com.fasterxml.jackson.annotation.JsonIgnore;");
//		topLevelClass.addJavaDocLine("import com.fasterxml.jackson.databind.annotation.JsonSerialize;");
//		topLevelClass.addJavaDocLine("import com.hanson.base.annotation.AutoWriteParam;");
//		topLevelClass.addJavaDocLine("import com.hanson.base.annotation.Describe;");
//		topLevelClass.addJavaDocLine("import com.hanson.base.mybatis.serializer.DatePattern;");
//		topLevelClass.addJavaDocLine("import com.hanson.base.serializer.EnumJsonSerializer;");

		topLevelClass.addJavaDocLine("import java.io.Serializable;");
		topLevelClass.addJavaDocLine("import com.baomidou.mybatisplus.annotation.TableId;");
		topLevelClass.addJavaDocLine("import com.baomidou.mybatisplus.annotation.IdType;");
		topLevelClass.addJavaDocLine("import com.baomidou.mybatisplus.annotation.TableField;");
		topLevelClass.addJavaDocLine("import com.baomidou.mybatisplus.annotation.TableName;");
		topLevelClass.addJavaDocLine("import lombok.*;");

		StringBuilder sb = new StringBuilder();

		topLevelClass.addJavaDocLine("/**");
		sb.append(" * ");
		sb.append(introspectedTable.getFullyQualifiedTable());
		sb.append(" ");
		sb.append(introspectedTable.getRemarks());
		topLevelClass.addJavaDocLine(sb.toString());

		sb.setLength(0);
		sb.append(" * @author ");
		sb.append("huhanlin");
		sb.append(" ");
		sb.append(currentDateStr);
		topLevelClass.addJavaDocLine(sb.toString());

		topLevelClass.addJavaDocLine(" */");
		topLevelClass.addJavaDocLine("@Data");
		topLevelClass.addJavaDocLine("@Builder");
		topLevelClass.addJavaDocLine("@NoArgsConstructor");
		topLevelClass.addJavaDocLine("@AllArgsConstructor");
		topLevelClass.addJavaDocLine("@EqualsAndHashCode(callSuper = true)");
		topLevelClass.addJavaDocLine("@TableName(\""+introspectedTable.getFullyQualifiedTable()+"\")");
    }
}
