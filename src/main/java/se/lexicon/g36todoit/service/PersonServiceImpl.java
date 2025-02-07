package se.lexicon.g36todoit.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.lexicon.g36todoit.dao.PersonDAO;
import se.lexicon.g36todoit.dao.TodoItemDAO;
import se.lexicon.g36todoit.model.dto.PersonDTO;
import se.lexicon.g36todoit.model.entity.AppUser;
import se.lexicon.g36todoit.model.entity.AppUserRole;
import se.lexicon.g36todoit.model.entity.Person;
import se.lexicon.g36todoit.model.entity.TodoItem;

import java.util.List;

@Service
public class PersonServiceImpl implements PersonService {

    private final PersonDAO personDAO;
    private final TodoItemDAO todoItemDAO;
    private final AppUserService appUserService;

    public PersonServiceImpl(PersonDAO personDAO, TodoItemDAO todoItemDAO, AppUserService appUserService) {
        this.personDAO = personDAO;
        this.todoItemDAO = todoItemDAO;
        this.appUserService = appUserService;
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Person create(PersonDTO dto, AppUserRole role){
        AppUser userCredentials = appUserService.create(dto.getAppUserForm(), role);
        Person newPerson = new Person(
                dto.getFirstName().trim(),
                dto.getLastName().trim()
        );
        newPerson.setUserCredentials(userCredentials);
        return personDAO.save(newPerson);
    }

    @Override
    @Transactional(readOnly = true)
    public Person findById(Integer id){
        return personDAO.findById(id)
                .orElseThrow();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Person> findAll(){
        return personDAO.findAll();
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Person update(String username, PersonDTO dto){
        Person original = personDAO.findByUserName(username).orElseThrow();
        original.setFirstName(dto.getFirstName().trim());
        original.setLastName(dto.getLastName().trim());
        return personDAO.save(original);
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean delete(Integer id){
        Person person = findById(id);
        for(TodoItem todoItem : person.getAssignedTodos()){
            todoItem.setAssignee(null);
        }
        person.setAssignedTodos(null);
        personDAO.delete(person);
        return !personDAO.existsById(id);
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Person assignTodoItem(Integer personId, Integer todoItemId){
        Person person = findById(personId);
        TodoItem todoItem = todoItemDAO.findById(todoItemId).orElseThrow();
        if(!person.getAssignedTodos().contains(todoItem)){
            person.getAssignedTodos().add(todoItem);
            todoItem.setAssignee(person);
        }
        return personDAO.save(person);
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Person removeTodoItem(Integer personId, Integer todoItemId){
        Person person = findById(personId);
        TodoItem todoItem = todoItemDAO.findById(todoItemId).orElseThrow();
        if(person.getAssignedTodos().contains(todoItem)){
            person.getAssignedTodos().remove(todoItem);
            todoItem.setAssignee(null);
        }
        return personDAO.save(person);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Person> findByNameContains(String search) {
        return personDAO.findByNameContains(search);
    }

    @Override
    @Transactional(readOnly = true)
    public Person findByUsername(String username) {
        return personDAO.findByUserName(username).orElseThrow();
    }
}
