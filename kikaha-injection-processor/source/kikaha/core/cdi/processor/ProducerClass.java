package kikaha.core.cdi.processor;

import static java.lang.String.format;

import javax.lang.model.element.*;
import java.util.List;
import kikaha.apt.*;
import kikaha.core.cdi.ProviderContext;

public class ProducerClass implements GenerableClass {

	final String packageName;
	final String methodName;
	final String returnType;
	final String typeName;
	final boolean expectsContext;
	final List<String> annotations;

	public ProducerClass(
			final String packageName,
			final String providedMethod, final String returnType,
			final String typeName,
			final boolean expectsContext,
			final List<String> annotations ) {
		this.packageName = NameTransformations.stripGenericsFrom( packageName );
		this.returnType = NameTransformations.stripGenericsFrom( returnType );
		this.methodName = NameTransformations.stripGenericsFrom( providedMethod );
		this.typeName = NameTransformations.stripGenericsFrom( typeName );
		this.expectsContext = expectsContext;
		this.annotations = annotations;
	}

	public int hashCode() {
		return format( "%s%s%s%s%s%s",
			packageName, methodName, returnType, typeName, expectsContext, annotations.hashCode() )
			.hashCode();
	}

	public String getMethodName() {
		return methodName;
	}

	@Override
	public String getTypeName() {
		return typeName;
	}

	@Override
	public String getPackageName() {
		return packageName;
	}

	@Override
	public String getGeneratedClassName() {
		return format("%sAutoGeneratedProvider%s", APT.extractTypeName( returnType ), String.valueOf( getIdentifier() ) );
	}

	public static ProducerClass from( final ExecutableElement method ) {
		final String type = APT.asType( method.getEnclosingElement() );

		return new ProducerClass(
				APT.extractPackageName( type ),
				method.getSimpleName().toString(),
				method.getReturnType().toString(),
				APT.extractTypeName( type ),
				measureIfExpectsContextAsParameter( method ),
				SingletonImplementation.getQualifierAnnotation(method) );
	}

	static boolean measureIfExpectsContextAsParameter( final ExecutableElement method ) {
		final List<? extends VariableElement> parameters = method.getParameters();
		if ( parameters.size() == 0 )
			return false;
		final VariableElement variableElement = parameters.get( 0 );
		if ( !variableElement.asType().toString().equals( ProviderContext.class.getCanonicalName() ) )
			throw new IllegalStateException(
					"@Provider annotated methods should have no parameters, or the parameter should be of type ProviderContext." );
		return true;
	}
}
