#if !defined IEntity_H
#define IEntity_H

#include <stdlib.h>
#include <string>

#include "sinks/ParameterReader.h"

class IEntity {
public:
    /**
     * Represents attributes within entities
     */
    template <typename T>
    class Attribute {
    public:
        T value;
        bool updated = false;

        Attribute (const T& value) : value(value), updated(false){}; // all attributes values are set, but start as not updated. If any attribute is updated has to be marked separately
        Attribute (const T& value, bool update) : value(value), updated(update){};
    };

	Attribute<std::string> id;

	IEntity(const std::string& id): id(id){
	}

	virtual ~IEntity() {}

	void printInfo(int level){
		printLog(level, "\t ID= %s \n", this->id.value.data());
	}


};




#endif
