class ApplicationController < ActionController::API
  include Concerns::SessionManagement
  include Concerns::ErrorHandling
end
