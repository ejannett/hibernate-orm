/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.dialect;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import oracle.jdbc.provider.oson.JacksonOsonConverter;
import oracle.sql.json.OracleJsonDatum;
import org.hibernate.metamodel.mapping.EmbeddableMappingType;
import org.hibernate.metamodel.spi.RuntimeModelCreationContext;
import org.hibernate.type.SqlTypes;
import org.hibernate.type.descriptor.ValueBinder;
import org.hibernate.type.descriptor.ValueExtractor;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.JavaType;
import org.hibernate.type.descriptor.jdbc.AggregateJdbcType;
import org.hibernate.type.descriptor.jdbc.BasicBinder;
import org.hibernate.type.descriptor.jdbc.BasicExtractor;
import org.hibernate.type.format.FormatMapper;

import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class OracleOsonJacksonJdbcType extends OracleJsonJdbcType {
	public static final OracleOsonJacksonJdbcType INSTANCE = new OracleOsonJacksonJdbcType( null );

	private OracleOsonJacksonJdbcType(EmbeddableMappingType embeddableMappingType) {
		super( embeddableMappingType );
	}

	public int getJdbcTypeCode() {
		return SqlTypes.JSON;
	}

	@Override
	public String toString() {
		return "OracleOsonJacksonJdbcType";
	}

	@Override
	public AggregateJdbcType resolveAggregateJdbcType(
			EmbeddableMappingType mappingType,
			String sqlType,
			RuntimeModelCreationContext creationContext) {
		return new OracleOsonJacksonJdbcType( mappingType );
	}

	@Override
	public <X> ValueBinder<X> getBinder(JavaType<X> javaType) {
		return new BasicBinder<>( javaType, this ) {

			final ObjectMapper objectMapper = JacksonOsonConverter.getObjectMapper();


			private <X> InputStream toOson(X value, JavaType<X> javaType, WrapperOptions options) throws Exception {
				FormatMapper mapper = options.getSession().getSessionFactory().getFastSessionServices()
						.getJsonFormatMapper();

				PipedOutputStream out = new PipedOutputStream();
				PipedInputStream in = new PipedInputStream(out);
				JsonGenerator osonGen = objectMapper.getFactory().createGenerator( out );
				mapper.writeToTarget( value, javaType, osonGen, options );
				osonGen.close();
				return in;
			}

			@Override
			protected void doBind(PreparedStatement st, X value, int index, WrapperOptions options)
					throws SQLException {
				try {
					st.setBinaryStream( index, toOson( value, getJavaType(), options ) );
				}
				catch (Exception e) {
					throw new SQLException( e );
				}
			}

			@Override
			protected void doBind(CallableStatement st, X value, String name, WrapperOptions options)
					throws SQLException {

				try {
					st.setBinaryStream( name, toOson( value, getJavaType(), options ) );
				}
				catch (Exception e) {
					throw new SQLException( e );
				}
			}
		};
	}

	@Override
	public <X> ValueExtractor<X> getExtractor(JavaType<X> javaType) {


		return new BasicExtractor<>( javaType, this ) {

			private X fromOson(byte[] osonBytes, FormatMapper mapper, WrapperOptions options) throws Exception {
				return mapper.readFromSource(  getJavaType(), osonBytes, options);
			}

			@Override
			protected X doExtract(ResultSet rs, int paramIndex, WrapperOptions options) throws SQLException {

				FormatMapper mapper = options.getSession().getSessionFactory().getFastSessionServices()
						.getJsonFormatMapper();

				OracleJsonDatum ojd = rs.getObject( paramIndex, OracleJsonDatum.class );
				if ( ojd == null ) {
					return null;
				}
				byte[] osonBytes = ojd.shareBytes();

				try {
					return fromOson( osonBytes, mapper ,options);
				}
				catch (Exception e) {
					throw new SQLException( e );
				}
			}

			@Override
			protected X doExtract(CallableStatement statement, int index, WrapperOptions options) throws SQLException {

				FormatMapper mapper = options.getSession().getSessionFactory().getFastSessionServices()
						.getJsonFormatMapper();

				OracleJsonDatum ojd = statement.getObject( index, OracleJsonDatum.class );
				if ( ojd == null ) {
					return null;
				}
				byte[] osonBytes = ojd.shareBytes();
				try {
					return fromOson( osonBytes, mapper ,options);
				}
				catch (Exception e) {
					throw new SQLException( e );
				}
			}

			@Override
			protected X doExtract(CallableStatement statement, String name, WrapperOptions options)
					throws SQLException {
				FormatMapper mapper = options.getSession().getSessionFactory().getFastSessionServices()
						.getJsonFormatMapper();

				OracleJsonDatum ojd = statement.getObject( name, OracleJsonDatum.class );
				if ( ojd == null ) {
					return null;
				}
				byte[] osonBytes = ojd.shareBytes();
				try {
					return fromOson( osonBytes, mapper ,options);
				}
				catch (Exception e) {
					throw new SQLException( e );
				}
			}

		};
	}


}
