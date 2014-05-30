describe("ResetPasswordController", function() {
   
    var ResetPasswordController, $rootScope, $httpBackend, $humane;

    beforeEach(function() {
        module('bridge');

        var $route = {current:{params:{sessionToken: "abc"}}};
        
        $humane = {
            confirm: jasmine.createSpy(),
            error: jasmine.createSpy()
        };
        module(function($provide) {
            $provide.value('$route', $route);
            $provide.value('$humane', $humane);
        });
    });
    
    beforeEach(inject(function($injector) {
        $rootScope = $injector.get('$rootScope');
        $httpBackend = $injector.get('$httpBackend');
        
        var $controller = $injector.get('$controller');
        createController = function() {
            return $controller('ResetPasswordController', {'$scope' : $rootScope });
        };
    }));
    afterEach(function() {
        $httpBackend.verifyNoOutstandingExpectation();
        $httpBackend.verifyNoOutstandingRequest();
    });

    function setupPOST() {
        // verify that the password is posted along with the session token in the header, 
        // as was extracted from the routing service.
        return $httpBackend.expectPOST('/api/auth/resetPassword', {password: "asb"}, function(headers) {
            return headers['Bridge-Session'] === "abc";
        });
    }

    it("only submits the password when form is valid", function() {
       ResetPasswordController = createController();
       $rootScope.resetPasswordForm = {$valid: false};
       $rootScope.change();
       expect($humane.confirm).not.toHaveBeenCalled();
       expect($humane.error).not.toHaveBeenCalled();
    });
    it("submitting wrong session token shows an error", function() {
        ResetPasswordController = createController();
        $rootScope.resetPasswordForm = {$valid: true};
        $rootScope.password = "asb";

        setupPOST().respond(500, {"payload":"Invalid session token (abc)"});
        $rootScope.change();
        $httpBackend.flush();
        expect($humane.error).toHaveBeenCalledWith('Invalid session token (abc)');
    });
    it("submitting correctly shows a confirmation message", function() {
        ResetPasswordController = createController();
        $rootScope.resetPasswordForm = {$valid: true};
        $rootScope.password = "asb";

        setupPOST().respond(200, {});
        $rootScope.change();
        $httpBackend.flush();
        expect($humane.confirm).toHaveBeenCalledWith('Your password has been changed.');
    });

});