package de.uniba.dsg.serverless.pipeline.service;

import de.uniba.dsg.serverless.pipeline.model.config.SetupConfig;
import de.uniba.dsg.serverless.pipeline.repo.SetupConfigRepository;
import de.uniba.dsg.serverless.pipeline.util.SeMoDeException;
import de.uniba.dsg.serverless.users.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import java.util.List;
import java.util.Optional;

/**
 * Wrapper to change the attributes of the user config class. If the changes are made directly in the model classes,
 * there occur json parsing errors.
 */
@Slf4j
@Service
@SessionScope
public class SetupService {

    private final SetupConfigRepository setupConfigRepository;

    @Autowired
    public SetupService(SetupConfigRepository setupConfigRepository) {
        this.setupConfigRepository = setupConfigRepository;
    }

    public void createSetup(String setupName, User owner) throws SeMoDeException {
        SetupConfig setupConfig = new SetupConfig(setupName, owner);
        this.setupConfigRepository.save(setupConfig);
    }

    /**
     * Returns the setup config by its name, otherwise throws an exception.
     *
     * @param setupName
     * @return
     * @throws SeMoDeException
     */
    public SetupConfig getSetup(String setupName) throws SeMoDeException {
        Optional<SetupConfig> config = this.setupConfigRepository.findById(setupName);
        if (config.isPresent()) {
            return config.get();
        } else {
            throw new SeMoDeException("Setup with name " + setupName + " is not present!!");
        }
    }

    public SetupConfig save(SetupConfig setupConfig) {
        return this.setupConfigRepository.save(setupConfig);
    }

    /**
     * Returns all setups, when the user is admin, otherwise only the setups he is the owner.
     *
     * @param user
     * @return
     */
    public List<SetupConfig> getSetups(User user) {
        if (user.isAdmin()) {
            return this.setupConfigRepository.findAll();
        } else {
            return this.setupConfigRepository.findByOwner(user);
        }
    }

    /**
     * Checks, if the user is also the owner of the setup or if he is an admin.
     * In both cases, he can access the setup otherwise he gets an error page (FORBIDDEN).
     *
     * @param setupName
     * @param user
     * @return
     */
    public boolean checkSetupAccessRights(String setupName, User user) {
        SetupConfig config = this.setupConfigRepository.findBySetupName(setupName);
        if (config != null && config.getOwner() != null && config.getOwner().equals(user)) {
            return true;
        }
        return user.isAdmin();
    }
}
