#if !defined Vector_packettcpsnd_h
#define Vector_packettcpsnd_h

#include "simulator.h"
#include "event.h"
#include "stdarg.h"

#include "engine.h"
#include "packettcpsnd.h"
#include "vector/VectorialModel.h"

/**
 * VectoriaModel template is not used
 * packettcpsnd has a few details with parameters (string params and params that depend on index) which makes it hard  to use VectoriaModel
 */
class Vector_packettcpsnd: public Simulator {
protected:
	Coupling* D0;
	std::shared_ptr<VectorialEvent> vec; // reference to last sent vectorial event. TODO: use unique_prt
	Event y;
	int scalarInstances;
	std::vector<std::string> scalarParameters;

	packettcpsnd* createScalarInstance(char* scalarModelName){
		return new packettcpsnd(scalarModelName);
	}

public:
	Vector_packettcpsnd(const char *n): Simulator(n) {};
	virtual ~Vector_packettcpsnd() { }
	int getParameterCount() { return 11;}

	/************ Implementation of Simulator methods  ************/
	void init(double t,...) {
		//The 'parameters' variable contains the parameters transferred from the editor.
		va_list parameters;
		va_start(parameters,t);

		// Get all scalar parameters names
		std::vector<std::string> scalarParamNames;
		for (int i = 0; i < this->getParameterCount(); i++) {
			scalarParamNames.push_back(va_arg(parameters, char*));
		}

		// read from the params the amount of scalar instances to be created
		char *fvar = va_arg(parameters,char*);
		scalarInstances = readDefaultParameterValue<int>(fvar);/*getScilabVar(fvar, true);*/

		//printLog(LOG_LEVEL_INIT, "[%f] %s_init: Number of instances %i  \n", t, this->getName(), scalarInstances);


        // read scalar parameters, each as a vector (where each element of the vector will be the parameter for scalar model i)
//		for (int i = 0; i < this->getParameterCount(); i++) {
//			scalarParameters.push_back(readDefaultParameterValue<std::string>(scalarParamNames[i].data()));
//			//printLog(LOG_LEVEL_INIT, "[%f] %s_init : Parameter %i=%s is a vector with %i values  \n", t, this->getName(), i, scalarParamNames[i].data(), scalarParameters[i].size());
//		}

		// create a coupled model that will contain all scalar model
		auto coupledName = std::string(this->getName()); //+ "_vector";
		D0 = new Coupling(coupledName.data());
		((Simulator*)D0)->father = this->father; // use same parent
		Simulator **D1 = new Simulator*[scalarInstances];
		Connection **EIC1 = new Connection*[0];
		Connection **EOC1 = new Connection*[0];
		Connection **IC1 = new Connection*[0];

		// create scalar models
		char* childi;
//		char* pars[this->getParameterCount()];
		for (int i = 0; i < scalarInstances; i++) {
			childi = new char[64];
			sprintf(childi, "%s_%i", "", i); // Using simple name as name for scalars. This will create the impression that the scalar models are in the hierarchy of the main simulation, but they are really not. They are hierarchically disconnected from the main simulation
			D1[i] = this->createScalarInstance(childi); //new DcmApp(childi);
			D1[i]->father = D0; // 'this' is an atomic model which creates separated simulation structure (top=D0). D0 will be the father of all scalars, but D0 will have no father as it is the top model. (TODO: the structure created by this model is not connected to the main simulation)

			// If is inherits from ScalarSimulator, set its scalarIndex
			if (ScalarSimulator* scalarModel = dynamic_cast<ScalarSimulator*>(D1[i])) {
				scalarModel->scalarIndex = i;
			}

			// get the parameters to pass to the scalar model
//			for (uint j = 0; j < this->getParameterCount(); j++) {
//				if (i < scalarParameters[j].size()) {
//					pars[j] = new char[24]; // save memory for the parameter name.
//					sprintf(pars[j], "%1.16g", scalarParameters.at(j).at(i)); // NOTE: we are doing this convertion because it is not supported to read parameters as strings from scilab
//					//printLog(LOG_LEVEL_FULL_LOGGING, "[%f] %s_init : Parameter %i for model %i =%s \n", t, this->getName(), j, i, pars[j]);
//				}
//			}

			// init the scalar model with corresponding params
			D1[i]->init(t, scalarParamNames.at(0).data(),
					scalarParamNames.at(1).data(),
					scalarParamNames.at(2).data(),
					std::to_string(i).data(), // port sender
					std::to_string(i).data(), // port receiver
					scalarParamNames.at(3).data(),
					scalarParamNames.at(4).data(),
					scalarParamNames.at(5).data(),
					scalarParamNames.at(6).data(),
					scalarParamNames.at(7).data(),
					scalarParamNames.at(8).data(),
					scalarParamNames.at(9).data(),
					scalarParamNames.at(10).data());
		}

		// setup and init coupled model
		D0->setup(D1, scalarInstances, IC1, 0, EIC1, 0, EOC1, 0);
		D0->init(t);
	}

	double ta(double t) {
		//This function returns a double.
		return D0->ta(t);
	}

	void dint(double t) {
		D0->dint(t);
	}

	void dext(Event x, double t) {
		auto vec1 = std::static_pointer_cast<VectorialEvent> (x.valuePtr);
		int index = vec1->index;

		//printLog(LOG_LEVEL_FULL_LOGGING, "[%f] %s: received event with index %i\n", t, this->getName(), index);

		// Set the scalar value to the event before forwarding
		x.valuePtr = vec1->valuePtr;

		if ((index>-1)&&(index<scalarInstances)){
			D0->D[index]->dextmessage(x,t);
			D0->heap.update(index);
		} else if (index==-1) {
			for (int ind=0;ind<scalarInstances;ind++){
				D0->D[ind]->dextmessage(x,t);
				D0->heap.update(ind);
			}
		}
	}

	Event lambda(double t) {
		//This function returns an Event:
		//     Event(%&Value%, %NroPort%)
		//where:
		//     %&Value% points to the variable which contains the value.
		//     %NroPort% is the port number (from 0 to n-1)

		y= D0->D[D0->transitionChild]->lambdamessage(t);

		vec = std::make_shared<VectorialEvent>();
		vec->valuePtr = y.valuePtr;
		vec->index=D0->transitionChild;

		y.valuePtr = vec;

		//printLog(LOG_LEVEL_FULL_LOGGING, "[%f] %s: Forwarding Vectorial Event message using index %u and port %u \n", t, this->getName(), vec->index, y.port);
		return y;
	}

	void exit(double t) {
		D0->exit(t);
	}

};
#endif
