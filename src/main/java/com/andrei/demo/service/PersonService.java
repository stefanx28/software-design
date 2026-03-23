package com.andrei.demo.service;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.Person;
import com.andrei.demo.model.PersonCreateDTO;
import com.andrei.demo.model.Problem;
import com.andrei.demo.repository.PersonRepository;
import com.andrei.demo.repository.ProblemRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class PersonService {
    private final PersonRepository personRepository;
    private final ProblemRepository problemRepository;
    public List<Person> getPeople() {
        return personRepository.findAll();
    }

    public Person addPerson(PersonCreateDTO personDTO) throws ValidationException {

        validatePerson(personDTO);
        Person person = new Person();

        person.setName(personDTO.getName());
        person.setAge(personDTO.getAge());
        person.setEmail(personDTO.getEmail());
        person.setPassword(personDTO.getPassword());

        return personRepository.save(person);
    }

    public Person updatePerson(UUID uuid, Person person) throws ValidationException{
        Optional<Person> personOptional =
                personRepository.findById(uuid);

        if(personOptional.isEmpty()) {
            throw new ValidationException("Person with id " + uuid + " not found");
        }
        Person existingPerson = personOptional.get();

        existingPerson.setName(person.getName());
        existingPerson.setAge(person.getAge());
        existingPerson.setEmail(person.getEmail());
        existingPerson.setPassword(person.getPassword());

        return personRepository.save(existingPerson);
    }

    public Person updatePerson2(UUID uuid, Person person) throws ValidationException{
        return personRepository
                        .findById(uuid)
                        .map(existingPerson -> {
                            existingPerson.setName(person.getName());
                            existingPerson.setAge(person.getAge());
                            existingPerson.setEmail(person.getEmail());
                            existingPerson.setPassword(person.getPassword());
                            return personRepository.save(existingPerson);
                        })
                        .orElseThrow(
                                () -> new ValidationException("Person with id " + uuid + " not found")
                        );
    }

    public Person patchPerson(UUID uuid, Person partial) throws ValidationException {
        Person existing = getPersonById(uuid);

        if (partial.getName() != null)
            existing.setName(partial.getName());

        if (partial.getEmail() != null)
            existing.setEmail(partial.getEmail());

        if (partial.getAge() != null)
            existing.setAge(partial.getAge());

        if (partial.getPassword() != null)
            existing.setPassword(partial.getPassword());

        return personRepository.save(existing);
    }

    public void deletePerson(UUID id) {
        Person person = getPersonById(id);


        for (Problem problem : person.getSolvedProblems()) {
            problem.getSolvers().remove(person);
            problemRepository.save(problem);
        }


        personRepository.delete(person);
    }



    public Person getPersonByEmail(String email) {
        return personRepository.findByEmail(email).orElseThrow(
                () -> new IllegalStateException("Person with email " + email + " not found"));
    }

    public Person getPersonById(UUID uuid) {
        return personRepository.findById(uuid).orElseThrow(
                () -> new IllegalStateException("Person with id " + uuid + " not found"));
    }


    private void validatePerson(PersonCreateDTO dto) throws ValidationException {


        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new ValidationException("Name cannot be empty");
        }
        if (!dto.getName().matches("^[a-zA-Z ]{2,50}$")) {
            throw new ValidationException("Name must be between 2 and 50 characters and contain only letters");
        }

        if (dto.getAge() == null) {
            throw new ValidationException("Age cannot be null");
        }
        if (dto.getAge() < 18 || dto.getAge() > 120) {
            throw new ValidationException("Age must be between 18 and 120");
        }

        if (dto.getEmail() == null || dto.getEmail().isBlank()) {
            throw new ValidationException("Email cannot be empty");
        }
        if (!dto.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new ValidationException("Email format is invalid");
        }

        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new ValidationException("Password cannot be empty");
        }


        if (personRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new ValidationException("Email '" + dto.getEmail() + "' is already in use");
        }
    }
}
